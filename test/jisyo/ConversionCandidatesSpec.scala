package skkserv.jisyo

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers._

class ConversionCandidatesSpec extends AnyWordSpec {

  "#append" should {
    "正しく追加する" in {
      val c1 = ConversionCandidates("a/b/c")
      val c2 = ConversionCandidates("b/d")

      val res = c1 append c2

      res.get mustBe Some("a/b/c/d")
    }
  }
}
