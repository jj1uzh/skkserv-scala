package skkserv

import skkserv.server.{Server => SKKServer}
import skkserv.jisyo.StaticJisyo
import scala.util.{Success, Failure, Using}
import java.net.ServerSocket
import skkserv.jisyo.TrieJisyo
import skkserv.jisyo.TrieJisyo.TrieNode

object Main {
  def main(args: Array[String]): Unit = {
    println("loading jisyoes...")

    import skkserv.jisyo.MapJisyo.MapIsStaticJisyo
    import skkserv.jisyo.TrieJisyo.TrieIsStaticJisyo
    val jisyoLEntries = StaticJisyo.entriesFromFile("""/usr/share/skk/SKK-JISYO.L""").get
    val jisyoZipEntries = StaticJisyo.entriesFromFile("""/usr/share/skk/SKK-JISYO.zipcode""").get

    val zipTrieJisyo = implicitly[StaticJisyo[TrieNode[Char, String]]].fromEntries(jisyoZipEntries)

    println("starting server...")

    SKKServer.run(port = 1178, zipTrieJisyo) match {
      case Failure(exception) => println(exception.getMessage())
      case Success(_)         => ()
    }

    println("bye.")
  }
}
