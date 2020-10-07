package skkserv

import org.scalatest.wordspec.AnyWordSpec

class JisyoFileSpec extends AnyWordSpec {

  "#convert" should {
    "convert properly" in {
      val jf = new JisyoFile(
        okuriAriEntries = Vector("あi /合/会/"),
        okuriNasiEntries = Vector("ai /エーアイ/", "あい /愛/相/")
      )

      assert(jf.convert("あi") == Some(Vector("合", "会")))
      assert(jf.convert("あい") == Some(Vector("愛", "相")))
      assert(jf.convert("ai") == Some(Vector("エーアイ")))
      assert(jf.convert("ない") == None)
    }
  }
}
