package skkserv.jisyo

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers._

class JisyoSpec extends AnyWordSpec {

  ".convert" should {
    "正しく変換" in {
      val jisyo = StaticJisyo(Map("abc" -> "def"))

      val result0 = Jisyo.convert(jisyo)("abc".toList)
      result0 mustBe Some("def")

      val result1 = Jisyo.convert(jisyo)("a".toList)
      result1 mustBe None
    }
  }
}
