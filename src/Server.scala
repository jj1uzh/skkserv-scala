package skkserv

import java.io.{OutputStreamWriter, PrintWriter}
import java.net.ServerSocket
import scala.concurrent.Future
import scala.io.{Codec, Source}
import scala.util.{Failure, Success, Using, Try}
import scala.util.control.Exception.{allCatch, ultimately}
import buildinfo.BuildInfo

import skkserv.jisyo.JisyoFile

case class Server(src: Source, printer: PrintWriter, jisyoFiles: Vector[JisyoFile]) {
  import Server.Request
  import Server.Request.{Close, Convert, Complete, Version, Hostname}

  private def send: String => Unit =
   (str) => { printer.print(str); printer.flush() }

  def run(): Unit =
    ultimately { send("0") } apply {
      Request from src takeWhile (_ != Close) collect {
        case Convert(midashi) =>
          (jisyoFiles flatMap (_ convert midashi)).flatten.distinct match {
            case v if v.size == 0 => "4\n"
            case v => s"1/${v mkString "/"}/\n"
          }
        case Complete(prefix) =>
          (jisyoFiles flatMap (_ complete prefix)).flatten.distinct match {
            case v if v.size == 0 => "4\n"
            case v => s"1/${v mkString "/"}/\n"
          }
        case Version => s"${BuildInfo.name}.${BuildInfo.version} "
        case Hostname => " " // unimplemented
      } foreach send
    }
}

final object Server {

  @inline private def loop(body: => Unit): Unit = while(true) body

  def runOnPort(port: Int, jisyoFiles: Vector[JisyoFile])(implicit codec: Codec): Try[Unit] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    allCatch toTry Using(new ServerSocket(port)) { listener => loop {
      val socket = listener.accept()
      Future {
        val inputStream = socket.getInputStream()
        val source = Source fromInputStream inputStream
        val outputStream = new OutputStreamWriter(socket.getOutputStream, codec.name)
        val printer = new PrintWriter(outputStream)
        Server(source, printer, jisyoFiles).run()
      } onComplete {
        case Success(_) => ()
        case Failure(e) => println(s"[warn] connection closed: ${e.getMessage()}")
      }
    }}
  }

  sealed trait Request
  object Request {
    final case object Close extends Request
    final case class Convert(midashi: String) extends Request
    final case object Version extends Request
    final case object Hostname extends Request
    final case class Complete(midashi: String) extends Request
    final case class Invalid(number: Char) extends Request

    def from(source: Source): Iterator[Request] =
      new Iterator[Request] {

        def hasNext: Boolean = source.hasNext

        private def nextUntilWhitespace: String =
          source.next() match {
            case ' ' => ""
            case ch  => ch +: nextUntilWhitespace
          }

        def next(): Request =
          source.next() match {
            case '0'   => Close
            case '1'   => Convert(nextUntilWhitespace)
            case '2'   => Version
            case '3'   => Hostname
            case '4'   => Complete(nextUntilWhitespace)
            case other => Invalid(other)
          }
      }
  }
}
