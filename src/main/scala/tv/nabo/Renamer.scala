package tv.nabo

import fabric.MergeType
import fabric.rw.RW

import java.io.File
import perfolation._
import profig._

import scala.util.matching.Regex

object Renamer {
  private[nabo] lazy val settings = Profig("renamer").as[Settings]

  val StandardMatcher: Regex = """.*[S[s]](\p{Digit}{2})\.?[E[e]](\p{Digit}{1,2})[. -]*(.*)[.].+""".r
  val LongMatcher: Regex = """.*Season (\p{Digit}{2}) Episode (\p{Digit}{2})[. -]*(.*)[.].+""".r
  val SimpleMatcher: Regex = """.*(\p{Digit}{1,2}+)x(\p{Digit}{1,2}+)[. -]*(.*)[.].+""".r
  val EpisodeOnlyMatcher: Regex = """Episode (\p{Digit}{2}) [-] (.+)[.].+""".r
  val FourDigitDoubleMatcher: Regex = """(.+) (\p{Digit}{2})(\p{Digit}{2})-(\p{Digit}{2}) (.+)[.].+?""".r
  val FourDigitMatcher: Regex = """(.+) (\p{Digit}{2})(\p{Digit}{2}) (.+)[.].+?""".r

  val matchers = List(
    partial {
      case StandardMatcher(season, episode, title) => Episode(title, season.toInt, episode.toInt)
    },
    partial {
      case LongMatcher(season, episode, title) => Episode(title, season.toInt, episode.toInt)
    },
    partial {
      case SimpleMatcher(season, episode, title) => Episode(title, season.toInt, episode.toInt)
    },
    partial {
      case EpisodeOnlyMatcher(episode, title) => Episode(title, settings.season, episode.toInt)
    },
    partial {
      case FourDigitDoubleMatcher(_, season, episode, _, title) => Episode(title, season.toInt, episode.toInt)
    },
    partial {
      case FourDigitMatcher(_, season, episode, title) => Episode(title, season.toInt, episode.toInt)
    }
  )

  def main(args: Array[String]): Unit = {
    Profig.initConfiguration()
    Profig.merge(args.toList, MergeType.Overwrite)

    scribe.info(s"Directory: ${settings.directory.getAbsolutePath}")
    settings.directory.listFiles().foreach { f =>
//      scribe.info(s"File: ${f.getName} / ${rename(f)}")
      rename(f) match {
        case Some(er) => if (settings.rename) {
          er.rename(settings.show)
        } else {
          val newFile = er.newFile(settings.show)
          scribe.info(s"Rename ${er.current.getName} to ${newFile.getName}")
        }
        case None => scribe.warn(s"No match for '${f.getName}'")
      }
    }
  }

  def rename(file: File): Option[EpisodeRename] = {
    matchers.to(LazyList).flatMap(r => r.unapply(file).map(EpisodeRename(file, _, r))).headOption
  }

  def partial(f: PartialFunction[String, Episode]): RenameMatcher = new RenameMatcher {
    override def unapply(file: File): Option[Episode] = f.lift(file.getName)
  }
}

trait RenameMatcher {
  def unapply(file: File): Option[Episode]
}

case class EpisodeRename(current: File,
                         episode: Episode,
                         renamer: RenameMatcher) {
  lazy val extension: String = {
    val n = current.getName
    val dot = n.lastIndexOf('.')
    n.substring(dot + 1)
  }

  def newFile(show: String): File = {
    val name = s"$show - s${episode.season.f(i = 2, f = 0)}e${(episode.number + Renamer.settings.increment).f(i = 2, f = 0)} - ${episode.title}.$extension"
    new File(current.getParentFile, name)
  }

  def rename(show: String): Boolean = {
    current.renameTo(newFile(show))
  }
}

case class Episode(title: String, season: Int, number: Int)

case class Settings(rename: Boolean = false,
                    show: String,
                    season: Int = 1,
                    directory: File,
                    increment: Int = 0)

object Settings {
  implicit val rw: RW[Settings] = RW.gen
}