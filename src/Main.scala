package skkserv

import scala.util.{Failure, Success}
import scala.io.Codec

object Main {

  final val configPath = s"""${System.getProperty("user.home")}/.config/skkserv-scala/config.json"""

  def main(args: Array[String]): Unit = {

    implicit val codec: Codec = Codec("EUC_JP")

    val config =
      Config(configPath) match {
        case Failure(exception) => {
          println(s"error loading config file: ${exception.getMessage()}")
          Config()
        }
        case Success(value) => value
      }

    val jisyoFiles =
      config.jisyoPlaces.foldRight(Nil: List[JisyoFile])((path, acc) =>
        JisyoFile fromFile path match {
          case Left(msg) => println(msg); acc
          case Right(jf) => jf :: acc
        }
      )
    jisyoFiles foreach println
    Server.run(1178, jisyoFiles) match {
      case Failure(exception) => println(exception)
      case Success(_)         => ()
    }

    println("bye.")
  }
}
