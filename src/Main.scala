package skkserv

import skkserv.server.{Server => SKKServer}
import skkserv.jisyo.StaticJisyo
import scala.util.{Success, Failure, Using}
import java.net.ServerSocket

object Main {
  def main(args: Array[String]): Unit = {
    println("loading jisyoes...")

    val jisyoL = StaticJisyo.fromFile("""/usr/share/skk/SKK-JISYO.L""").get //getOrElse StaticJisyo(Map())
    val zipJisyo = StaticJisyo.fromFile("""/usr/share/skk/SKK-JISYO.zipcode""").get // getOrElse StaticJisyo(Map())
    val testJisyo = StaticJisyo(Map("てすと" -> "skkserv-scala running"))

    println("starting server...")

    SKKServer.run(port = 1178, zipJisyo) match {
      case Failure(exception) => println(exception.getMessage())
      case Success(_)         => ()
    }

    println("bye.")
  }
}
