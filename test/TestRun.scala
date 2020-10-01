package skkserv

import java.io.PrintWriter
import scala.io.{Codec, Source}

object TestRun extends App {

  implicit val codec: Codec = Codec("EUC_JP")

  val config = Config(Main.configPath).get
  val jisyoFiles =
    config.jisyoPlaces flatMap { path =>
      JisyoFile fromFile path match {
        case Left(msg) => println(msg); None
        case Right(jf) => Some(jf)
      }
    }

  Server(Source.stdin, new PrintWriter(System.out), jisyoFiles).run()
}
