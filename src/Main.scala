package skkserv

import scala.util.{Failure, Success}
import scala.io.Codec

import skkserv.jisyo.JisyoFile

object Main {

  final val configPath = s"""${System.getProperty("user.home")}/.config/skkserv-scala/config.json"""

  def main(args: Array[String]): Unit = {

    implicit val codec: Codec = Codec("EUC_JP")

    print("loading config...")
    val config =
      Config(configPath) match {
        case Failure(exception) =>
          println(s"error loading config file: ${exception.getMessage()}")
          Config()

        case Success(value) => value
      }

    println(s"loading jisyo files...")
    val jisyoFiles =
      config.jisyoPlaces flatMap { path =>
        JisyoFile fromFile path match {
          case Left(msg) => println(msg); None
          case Right(jf) => Some(jf)
        }
      }

    println("starting server...")
    Server.runOnPort(1178, jisyoFiles) match {
      case Failure(e) => println(s"[error] terminated: ${e.getMessage()}")
      case Success(_) => ()
    }

    println("bye.")
  }
}
