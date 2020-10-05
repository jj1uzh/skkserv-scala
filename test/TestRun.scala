package skkserv

import java.io.PrintWriter
import scala.io.{Codec, Source}
import org.scalatest.wordspec.AnyWordSpec

class TestRun extends AnyWordSpec {

  "TestRun" should {
    "run properly" in {

      implicit val codec: Codec = Codec("EUC_JP")

      val jisyoPaths = Vector("/usr/share/skk/SKK-JISYO.L", "/usr/share/skk/SKK-JISYO.station")
      val jisyoFiles =
        jisyoPaths flatMap { path =>
          JisyoFile fromFile path match {
            case Left(msg) => println(msg); None
            case Right(jf) => Some(jf)
          }
        }

      val request = "2\n1あいおい 1おおいまち 1わこう 0"
      Server(Source fromString request, new PrintWriter(System.out), jisyoFiles).run()
    }
  }
}
