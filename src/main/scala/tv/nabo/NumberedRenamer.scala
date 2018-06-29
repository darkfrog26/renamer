package tv.nabo

import java.io.File

import perfolation._

object NumberedRenamer {
  private lazy val EpisodeRegex = """.+[eE](\d+)(.+)[.](.+)""".r

  def main(args: Array[String]): Unit = {
    val directory = new File("/fileserver/videos/tv/Kyle XY")
    rename(directory, "Kyle XY", List(10, 23, 10), testFlight = false)
  }

  def clean(s: String): String = s.replaceAllLiterally(".", " ").trim

  def rename(directory: File, show: String, episodes: List[Int], testFlight: Boolean): Unit = {
    val files = directory.listFiles().toList.flatMap { file =>
      file.getName match {
        case EpisodeRegex(episode, name, extension) => {
          Some(Episode(show, episode.toInt, clean(name), extension.toLowerCase, file))
        }
        case _ => None
      }
    }.sortBy(_.index)
    val numbers = episodes.zipWithIndex.flatMap {
      case (eps, season) => {
        val s = season + 1
        (1 to eps).toList.map(e => s -> e)
      }
    }
    files.zip(numbers).foreach {
      case (episode, (seasonNumber, episodeNumber)) => {
        val num = s"s${seasonNumber.f(i = 2, f = 0)}e${episodeNumber.f(i = 2, f = 0)}"
        val name = s"$show - $num - ${episode.name}.${episode.extension}"
        val season = new File(directory, s"Season $seasonNumber")
        season.mkdirs()
        val file = new File(season, name)
        scribe.info(s"Rename ${episode.file.getName} to ${file.getName}")
        if (!testFlight) {
          episode.file.renameTo(file)
        }
      }
    }
  }

  case class Episode(show: String, index: Int, name: String, extension: String, file: File)
}