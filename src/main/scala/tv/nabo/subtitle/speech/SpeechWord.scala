package tv.nabo.subtitle.speech

import perfolation._

object SpeechWord {
  private val Minute: Double = 60.0
  private val Hour: Double = Minute * 60.0

  private def format(time: Double): String = {
    var value = time
    val hours = (value / Hour).toInt
    value = value % Hour
    val minutes = (value / Minute).toInt
    value = value % Minute
    val seconds = value.toInt
    value = value % 1.0
    val milliseconds = (value * 1000.0).toInt
    p"${hours.f(i = 2, f = 0)}:${minutes.f(i = 2, f = 0)}:${seconds.f(i = 2, f = 0)},${milliseconds.f(i = 3, f = 0)}"
  }
}

case class SpeechWord(endTime: String, startTime: String, word: String) {
  lazy val start: Double = startTime.substring(0, startTime.length - 1).toDouble
  lazy val startFormat: String = SpeechWord.format(start)
  lazy val end: Double = endTime.substring(0, endTime.length - 1).toDouble
  lazy val endFormat: String = SpeechWord.format(end)
}