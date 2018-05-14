//package com.outr.renamer
//
//import java.io.File
//
//import scala.annotation.tailrec
//
//object Renamer {
//  val rename = false
//  val test = false
//  val base = new File("""/Volumes/storage/videos/tv/Try/Reno 911! Complete/""")
//  val start = new File(base, "Reno.911.Season.01.XviD.DVD")
//  val extensions = List("avi", "mpg", "m4v", "mkv")
//  // *S00E00.EpisodeName
//  val StandardMatcher =
//    """.*[S[s]](\p{Digit}{2})\.?[E[e]](\p{Digit}{1,2})[. -]*(.*)(\.\S{3,4}+)""".r
//  // *S00 00 - EpisodeName
//  val NonStandardMatcher =
//    """.*[S[s]](\p{Digit}{1,2})[. -](\p{Digit}{2})[. -]*(.*)(\.\S{3,4}+)""".r
//  // * - Season 00 Episode 00 - EpisodeName
//  val LongMatcher =
//    """.*[. -]*Season (\p{Digit}{1,2}) Episode (\p{Digit}{1,2})[. -]*(.*)(\.\S{3,4}+)""".r
//  // *00x00 EpisodeName
//  val SimpleMatcher =
//    """.*(\p{Digit}{1,2}+)x(\p{Digit}{1,2}+)[. ](.*)(\.\S{3,4}+)""".r
//  // *[00x00] EpisodeName
//  val BracketMatcher =
//    """.*\[(\p{Digit}{1,2}+)x(\p{Digit}{1,2}+)\][. -]*(.*)(\.\S{3,4}+)""".r
//  // *0000 - EpisodeName
//  val CombineMatcher =
//    """.*?(\p{Digit}{1,2})(\p{Digit}{2})[. -]*(.*)(\.\S{3,4}+)""".r
//  // 00
//  val EpisodeMatcher =
//    """(\p{Digit}{1,2}+)(\.\S{3,4}+)""".r
//  // 00 EpisodeName
//  val NoSeasonMatcher =
//    """(\p{Digit}{1,2}+)\.?[. -](.*)(\.\S{3,4}+)""".r
//  // EpisodeName 00
//  val NoSeasonMatcher2 =
//    """(.*)[. -](\p{Digit}{2}+)(\.\S{3,4}+)""".r
//  val BonusEpisodeMatcher = """(.*)Bonus(.*)(\.\S{3,4}+)""".r
//  val ExtraEpisodeMatcher = """(.*)Extra(.*)(\.\S{3,4}+)""".r
//  val InterviewEpisodeMatcher = """(.*)Interview(.*)(\.\S{3,4}+)""".r
//  val TourEpisodeMatcher = """(.*)Tour(.*)(\.\S{3,4}+)""".r
//  val SeasonMatcher = """.*[S[s]]eason (\p{Digit}{1,2}+)""".r
//  val testValues = List("Frasier.S01E01.The Good Son.avi",
//    "S05E02 - The Gift Horse.avi",
//    "Frasier.s02e00.It's.a.girl.thing.DVDRip[ShareTV].avi",
//    "Frasier.s02.e00.Marching.on.to.season.two.DVDrip[ShareTV].avi",
//    "S10E18 - Frasier - Roe To Perdition.mpg",
//    "S10E15 - Frasier - Trophy Girlfriend.mpg",
//    "206 - The Fight.avi",
//    "Simple 01x02 Test Simple Episode.mpg",
//    "Combine 0102 Test Combine Episode.mpg",
//    "01.mpg",
//    "02 Test No Season Episode.mpg",
//    "Castle [1x04] Hell Hath No Fury.mkv",
//    "No Season 2 Episode 15.m4v",
//    "Diagnosis Murder - Season 1 Episode 3 - Murder At The Telethon.avi.avi"
//  )
//  var ignoreNoMatch = false // Don't throw an error on no-match files when ignore.rename exists
//
//  def mainOld(args: Array[String]): Unit = {
//    if (test) {
//      testMatching()
//    } else {
//      processFile(base)
//    }
//  }
//
//  def testMatching() = {
//    for (t <- testValues) {
//      val f = new File(t)
//      println("match: " + matchFile(f).getOrElse(null).toString.trim)
//    }
//  }
//
//  def matchFile(file: File) = {
//    // Generate partial functions
//    val simpleMatcher: PartialFunction[String, Option[EpisodeRename]] = {
//      case SimpleMatcher(season, episode, title, extension) => generateNewName(file, season, episode, title, extension, "SimpleMatcher")
//    }
//    val bracketMatcher: PartialFunction[String, Option[EpisodeRename]] = {
//      case BracketMatcher(season, episode, title, extension) => generateNewName(file, season, episode, title, extension, "BracketMatcher")
//    }
//    val standardMatcher: PartialFunction[String, Option[EpisodeRename]] = {
//      case StandardMatcher(season, episode, title, extension) => generateNewName(file, season, episode, title, extension, "StandardMatcher")
//    }
//    val longMatcher: PartialFunction[String, Option[EpisodeRename]] = {
//      case LongMatcher(season, episode, title, extension) => generateNewName(file, season, episode, title, extension, "LongMatcher")
//    }
//    val nonStandardMatcher: PartialFunction[String, Option[EpisodeRename]] = {
//      case NonStandardMatcher(season, episode, title, extension) => generateNewName(file, season, episode, title, extension, "NonStandardMatcher")
//    }
//    val combineMatcher: PartialFunction[String, Option[EpisodeRename]] = {
//      case CombineMatcher(season, episode, title, extension) => generateNewName(file, season, episode, title, extension, "CombineMatcher")
//    }
//    val episodeMatcher: PartialFunction[String, Option[EpisodeRename]] = {
//      case EpisodeMatcher(episode, extension) => generateNewName(file, determineSeason(file), episode, "", extension, "EpisodeMatcher")
//    }
//    val noSeasonMatcher: PartialFunction[String, Option[EpisodeRename]] = {
//      case NoSeasonMatcher(episode, title, extension) => generateNewName(file, determineSeason(file), episode, title, extension, "NoSeasonMatcher")
//    }
//    val noSeasonMatcher2: PartialFunction[String, Option[EpisodeRename]] = {
//      case NoSeasonMatcher2(title, episode, extension) => generateNewName(file, determineSeason(file), episode, title, extension, "NoSeasonMatcher2")
//    }
//    val bonusEpisodeMatcher: PartialFunction[String, Option[EpisodeRename]] = {
//      case BonusEpisodeMatcher(season, title, extension) => None
//    }
//    val extraEpisodeMatcher: PartialFunction[String, Option[EpisodeRename]] = {
//      case ExtraEpisodeMatcher(season, title, extension) => None
//    }
//    val interviewEpisodeMatcher: PartialFunction[String, Option[EpisodeRename]] = {
//      case InterviewEpisodeMatcher(season, title, extension) => None
//    }
//    val tourEpisodeMatcher: PartialFunction[String, Option[EpisodeRename]] = {
//      case TourEpisodeMatcher(season, title, extension) => None
//    }
//    val matcher = simpleMatcher orElse bracketMatcher orElse standardMatcher orElse longMatcher orElse
//      nonStandardMatcher orElse combineMatcher orElse episodeMatcher orElse noSeasonMatcher orElse
//      noSeasonMatcher2 orElse bonusEpisodeMatcher orElse extraEpisodeMatcher orElse
//      interviewEpisodeMatcher orElse tourEpisodeMatcher
//    try {
//      matcher(file.getName)
//    } catch {
//      case err: MatchError => if (ignoreNoMatch) None else error("Match Error: " + file.getAbsolutePath)
//    }
//  }
//
//  def processFile(file: File): Option[EpisodeRename] = {
//    if (file.isFile) {
//      if (validExtension(file)) {
//        matchFile(file)
//      } else {
//        //				println("Not valid: " + file.getName)
//        None
//      }
//    } else {
//      val skip = new File(file, "skip.rename")
//      if (!skip.exists) {
//        val ignore = new File(file, "ignore.rename")
//        ignoreNoMatch = ignore.exists
//        println(s"Processing: ${file.getAbsolutePath}...")
//        val results = file.listFiles.toList.map(processFile).collect {
//          case Some(er) => er
//        }
//        if (results.length > 0) { // Episodes found
//          println(results.length + " Episodes Found")
//          results.foreach(println)
//          val zero = hasEpisode(0, results)
//          val allEpisodesFound = (0 until results.length).foldLeft(true)((b, index) => {
//            if (b) {
//              hasEpisode(if (zero) index else index + 1, results)
//            } else {
//              false
//            }
//          })
//          if (!allEpisodesFound) {
//            error("Not all episodes found: " + file.getAbsolutePath)
//          } else if (rename) { // Actually modify the filenames
//            results.foreach(er => if (er.shouldRename) er.rename())
//          }
//        }
//        None
//      } else {
//        None
//      }
//    }
//  }
//
//  def findEpisode(episode: Int, results: List[EpisodeRename]) = results.find(er => er.episode == episode)
//
//  def determineSeason(file: File) = {
//    if (test) {
//      "1"
//    } else {
//      file.getParentFile.getName match {
//        case SeasonMatcher(season) => season
//        case _ => throw new RuntimeException("No match for: " + file.getName)
//      }
//    }
//  }
//
//  def generateNewName(file: File, season: String, episode: String, title: String, extension: String, matcher: String) = {
//    //    println("\tUsing: " + matcher)
//    var t = title.trim
//    //		if (t.indexOf('-') != -1) {
//    //			t = t.substring(t.lastIndexOf('-') + 1)
//    //		}
//    //		if (t.indexOf('[') != -1) {
//    //			t = t.substring(0, t.indexOf('['))
//    //		}
//    //		if (t.toLowerCase.endsWith("dvdrip")) {
//    //			t = t.substring(0, t.length - 6)
//    //		}
//    //		if (t.toLowerCase.endsWith("swenet")) {
//    //			t = t.substring(0, t.length - 6)
//    //		}
//    //		if (t.toLowerCase.endsWith("xor")) {
//    //			t = t.substring(0, t.length - 3)
//    //		}
//    //		if (t.toLowerCase.endsWith("hdtv.xvid.")) {
//    //			t = t.substring(0, t.length - 10)
//    //		}
//    //		if (t.toLowerCase.endsWith("lol")) {
//    //			t = t.substring(0, t.length - 3)
//    //		}
//    //		if (t.toLowerCase.endsWith("notv")) {
//    //			t = t.substring(0, t.length - 4)
//    //		}
//    //		if (t.endsWith(".")) {
//    //			t = t.substring(0, t.length - 1)
//    //		}
//    //		t = t.replaceAll("[.]", "")
//    if (t.length > 0) {
//      t = "." + t
//    }
//    val s = season.length match {
//      case 1 => "0" + season
//      case 2 => season
//    }
//    val e = episode.length match {
//      case 1 => "0" + episode
//      case 2 => episode
//    }
//    val showName = if (test) "test" else determineShowName(file)
//    val episodeName = removeExtraSpace(showName + ".S" + s + "E" + e + t + extension)
//    val newFile = new File(file.getParentFile, episodeName)
//    Some(EpisodeRename(file, newFile, season, episode.toInt, title, matcher))
//  }
//
//  def validExtension(file: File) = extensions.find((s: String) => file.getName.endsWith(s)) != None
//
//  private def hasEpisode(episode: Int, results: List[EpisodeRename]) = {
//    findEpisode(episode, results) match {
//      case Some(ep) => true
//      case None => {
//        findEpisode(episode - 1, results) match {
//          case Some(pep) => {
//            val ampersand = pep.title.indexOf('&')
//            val episodePosition = pep.title.indexOf(episode.toString)
//            if (ampersand > -1 && episodePosition > ampersand) {
//              true
//            } else if (pep.title.startsWith("E" + episode) || pep.title.startsWith("E0" + episode)) {
//              true
//            } else {
//              println("*** Cannot find episode: " + episode + " - Previous: " + pep.title)
//              false
//            }
//          }
//          case None => false
//        }
//      }
//    }
//  }
//
//  private def removeExtraSpace(name: String) = {
//    val b = new StringBuilder
//    var space = false
//    for (c <- name.trim) {
//      if (c == ' ') {
//        if (space) {
//          // Ignore
//        } else {
//          b.append(c)
//        }
//        space = true
//      } else {
//        space = false
//        b.append(c)
//      }
//    }
//    b.toString
//  }
//
//  @tailrec
//  private def determineShowName(file: File): String = {
//    val parent = file.getParentFile
//    if (parent.getParentFile == base) {
//      val name = parent.getName
//      val index = name.toLowerCase.indexOf("season")
//      if (index > -1) {
//        name.substring(0, index - 1)
//      } else {
//        name
//      }
//    } else {
//      determineShowName(parent)
//    }
//  }
//
//  case class EpisodeRename(current: File, modify: File, season: String, episode: Int, title: String, matcher: String) {
//    override def toString() = {
//      val b = new StringBuilder("\t")
//      b.append(current.getName)
//      if (shouldRename) {
//        b.append(" (Rename to: ")
//        b.append(modify.getName)
//        b.append(")")
//      }
//      b.append(" Season: ")
//      b.append(season)
//      b.append(", Episode: " + episode)
//      b.append(", Matcher: " + matcher)
//      b.toString
//    }
//
//    def shouldRename = current.getName != modify.getName
//
//    def rename() = {
//      if (!current.renameTo(modify)) error("Unable to rename: " + current.getAbsolutePath + " to " + modify.getName)
//    }
//  }
//}