package skkserv.server

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers._
import scala.io.Source
import java.io.{ByteArrayInputStream, InputStreamReader, InputStream, StringWriter, PrintWriter}

class ServerSpec extends AnyWordSpec {

  "#listen" should {
    "正しくレスポンスを返す" in {
      val req = """1a """
      val res = new StringWriter()
      val jisyo = skkserv.jisyo.StaticJisyo(Map("a" -> "b"))
      val server = Server(new InputStreamReader(new ByteArrayInputStream(req.getBytes())), new PrintWriter(res), jisyo)

      server.start()

      res.toString() mustEqual "1/b/\n"
    }
  }
}
