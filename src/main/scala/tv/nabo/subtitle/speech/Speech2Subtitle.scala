package tv.nabo.subtitle.speech

import java.io.{File, PrintWriter}

import profig.JsonUtil

import scala.io.Source

//ffmpeg -i Mad.About.You.S02E21.DVDRip.XviD-VF.avi -vn -acodec flac -ac 1 -sample_fmt s16p -ar 16000 output-audio.flac
//gcloud ml speech recognize-long-running gs://nabo-temp/output-audio.flac --language-code=en-US --include-word-time-offsets
object Speech2Subtitle {
  def process(inputJSON: File, outputSRT: File): Unit = {
    val jsonString = {
      val source = Source.fromFile(inputJSON)
      try {
        source.mkString
      } finally {
        source.close()
      }
    }
    val recognition = JsonUtil.fromJsonString[SpeechRecognition](jsonString)

    outputSRT.delete()
    val writer = new PrintWriter(outputSRT)
    recognition.results.zipWithIndex.foreach {
      case (block, index) => {
        val alternative = block.alternatives.head
        val transcript = alternative.transcript
        if (index > 0) {
          writer.println("")
        }
        writer.println(index + 1)
        val start = alternative.words.head.startFormat
        val end = alternative.words.last.endFormat
        writer.println(s"$start --> $end")
        writer.println(transcript)
      }
    }
    writer.flush()
    writer.close()
  }
}