package skkserv

import skkserv.Server
import skkserv.jisyo._
import TrieJisyo.Trie
import scala.util.{Success, Failure, Using}
import java.net.ServerSocket
import java.time.LocalDateTime

object Main {
  def main(args: Array[String]): Unit = {
    println("loading jisyoes...")

    val jisyoL = Jisyo.entriesFromFile("""/usr/share/skk/SKK-JISYO.L""").get
    val jisyoZip = Jisyo.entriesFromFile("""/usr/share/skk/SKK-JISYO.zipcode""").get
    val jisyoNow = Map(
      "いま" -> Seq(() => LocalDateTime.now.toString)
    ).transform { case (_, c) => ConversionCandidates(c) }
    val merged = List(jisyoL, jisyoZip, jisyoNow).reduce(Jisyo.merge)

    println("starting server...")

    Server.run(port = 1178, TrieJisyo(merged)) match {
      case Failure(exception) => println(exception.getMessage())
      case Success(_)         => ()
    }

    println("bye.")
  }
}
