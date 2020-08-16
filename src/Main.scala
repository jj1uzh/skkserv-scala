package skkserv

import skkserv.Server
import skkserv.jisyo._
import TrieJisyo.Trie
import scala.util.{Success, Failure, Using}
import java.net.ServerSocket
import java.time.LocalDateTime
import io.circe.parser.parse
import scala.io.Source
import io.circe.Json
import io.circe.parser.decode
import io.circe.generic.auto._
import scala.util.control.Exception.allCatch

object Main {

  final val configPath = s"""${System.getProperty("user.home")}/.config/skkserv-scala/config.json"""

  def main(args: Array[String]): Unit = {
    try {
      println("> loading config... <")
      val configStr = Using(Source.fromFile(configPath))(_.getLines.mkString).get
      val config = decode[Config](configStr) getOrElse {
        println("error loading config! uses default"); Config()
      }

      println("> loading jisyo files... <")
      val entriesList = config.jisyoFiles
        .map { name =>
          println(s"- $name")
          Using(Source.fromFile(s"""${config.jisyoDir}/$name""", "EUC-JP"))(src =>
            Jisyo.entriesFromSource(name, src)
          ) match {
            case Success(e)   => e
            case Failure(exp) => println(s"==> failed: ${exp.getMessage}"); Jisyo.emptyEntries
          }
        }

      println("> merging jisyoes... <")
      val mergedEntries = entriesList.reduce(Jisyo.merge)

      val jisyo = config.jisyoType match {
        case "hash" => MapJisyo(mergedEntries)
        case "trie" => TrieJisyo(mergedEntries)
        case other  => println(s"""unknown jisyo type $other. uses "hash"""""); MapJisyo(mergedEntries)
      }

      println("> starting server... <")
      Server.run(port = 1178, jisyo) match {
        case Failure(e) => throw e
        case Success(_) => ()
      }
    } catch {
      case _: InterruptedException => ()
      case e: Throwable            => println(e.getMessage)
    }

    println("bye.")
  }
}
