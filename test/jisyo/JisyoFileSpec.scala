package skkserv.jisyo

import org.scalatest.wordspec.AnyWordSpec

class JisyoFileSpec extends AnyWordSpec {
  import JisyoFile.Entry

  "#convert" should {
    "convert properly" in {
      val jf = new JisyoFile(
        okuriAriEntries = Vector(Entry("あi /合/会/")),
        okuriNasiEntries = Vector(Entry("ai /エーアイ/"), Entry("あい /愛/相/"))
      )
      assert(jf.convert("あi") == Some(Vector("合", "会")))
      assert(jf.convert("あい") == Some(Vector("愛", "相")))
      assert(jf.convert("ai") == Some(Vector("エーアイ")))
      assert(jf.convert("ない") == None)
    }
  }

  "#complete" should {
    "complete properly" in {
      val jf = new JisyoFile(
        okuriNasiEntries = Vector(
          "ああ /aa/",
          "あい /ai/",
          "あいう /aiu/",
          "あいこ /aiko/"
        ).map(Entry),
        okuriAriEntries = Vector.empty
      )
      assert(jf.complete("ほげ") == None)
      assert(jf.complete("あい") == Some(Vector("あい", "あいう", "あいこ")))
    }
  }

  ".Midashi" should {
    import JisyoFile.Midashi
    "# ^<=>:" should {
      "compare properly" in {
        assert(("あか" ^<=>: Midashi("ほげ")) < 0)
        assert(("ほげ" ^<=>: Midashi("あか")) > 0)
        assert(("あか" ^<=>: Midashi("あか")) == 0)
        assert(("あか" ^<=>: Midashi("あかい")) == 0)
      }
    }
  }
}
