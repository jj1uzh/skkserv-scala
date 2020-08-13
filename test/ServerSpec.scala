package skkserv

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers._
import scala.io.Source
import java.io.{ByteArrayInputStream, InputStreamReader, InputStream, StringWriter, PrintWriter}
import skkserv.jisyo.StaticMapJisyo

class ServerSpec extends AnyWordSpec {

  "#listen" should {
    "正しくレスポンスを返す" in {
      val req = """1a """
      val res = new StringWriter()
      val jisyo = StaticMapJisyo(Map("a" -> "b"))
      val server = Server(Source.fromInputStream(new ByteArrayInputStream(req.getBytes())), new PrintWriter(res), jisyo)

      server.run()

      res.toString() mustEqual "1/b/\n"
    }
  }
}
