package skkserv

import skkserv.server.{Server => SKKServer}
import skkserv.jisyo.StaticJisyo
import scala.util.{Success, Failure, Using}
import java.net.ServerSocket

object Main {
  def main(args: Array[String]): Unit = {
    println("hello.")

    val jisyoL = StaticJisyo.fromFile("""/usr/share/skk/SKK-JISYO.L""") match {
      case Failure(exception) => throw exception
      case Success(value)     => value
    }
    val testJisyo = StaticJisyo(Map("てすと" -> "skkserv-scala running"))
    val jisyo = StaticJisyo.merge(List(testJisyo, jisyoL))

    SKKServer.run(port = 1178, jisyo) match {
      case Failure(exception) => println(exception.getMessage())
      case Success(_)         => ()
    }

    println("bye.")
  }
}
