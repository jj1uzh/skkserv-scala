package skkserv

import java.io.{OutputStreamWriter, PrintWriter}
import java.net.ServerSocket
import scala.annotation.tailrec
import scala.io.{Codec, Source}
import scala.util.{Failure, Success, Using, Try}
import scala.util.control.Exception.noCatch
import scala.util.chaining.scalaUtilChainingOps
import buildinfo.BuildInfo

case class Server(src: Source, printer: PrintWriter, jisyoFiles: Vector[JisyoFile]) {
  import Server.Request
  import Server.Request.{Close, Convert, Complete, Version, Hostname}

  private def send: String => Unit = (str) => { printer.print(str); printer.flush() }

  def run(): Unit =
    noCatch andFinally send("0") apply {
      Request from src takeWhile (_ != Close) collect {
        case Convert(midashi) =>
          (jisyoFiles flatMap (_ convert midashi)).distinct match {
            case v if v.size == 0 => "4\n"
            case v                => s"1/${v mkString "/"}/\n"
          }
        case Complete(_) => "4\n" // unimplemented
        case Version     => s"${BuildInfo.name}.${BuildInfo.version} "
        case Hostname    => "" // unimplemented
      } foreach send
    }
}

final object Server {

  @tailrec
  def run(port: Int, jisyoFiles: Vector[JisyoFile])(implicit codec: Codec): Try[Unit] = {
    Using.Manager { use =>
      val serverSocket = use(new ServerSocket(port))
      val socket = use(serverSocket.accept())
      val inputStream = use(socket.getInputStream())
      val source = use(Source fromInputStream inputStream)
      val outputStream = use(new OutputStreamWriter(socket.getOutputStream, codec.name))
      val printer = use(new PrintWriter(outputStream))
      Server(source, printer, jisyoFiles).run()
    } match {
      case f @ Failure(_) => f
      case Success(_)     => run(port, jisyoFiles)
    }
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
