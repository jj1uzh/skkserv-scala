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

    val jisyoLEntries = Jisyo.entriesFromFile("""/usr/share/skk/SKK-JISYO.L""").get
    val jisyoZipEntries = Jisyo.entriesFromFile("""/usr/share/skk/SKK-JISYO.zipcode""").get
    val zipTrieJisyo = StaticTrieJisyo(Trie.fromEntries(jisyoZipEntries))
    val testTrieJisyo = DynamicMapJisyo(
      Map(
        "いま" -> (() => LocalDateTime.now.toString)
      )
    )

    println("starting server...")

    Server.run(port = 1178, testTrieJisyo) match {
      case Failure(exception) => println(exception.getMessage())
      case Success(_)         => ()
    }

    println("bye.")
  }
}
