package skkserv.server

import buildinfo.BuildInfo
import scala.io.Source
import scala.util.{Using, Try}
import java.io.{OutputStreamWriter, PrintWriter}
import java.net.ServerSocket
import skkserv.jisyo.StaticJisyo

case class Server[T] private (source: Source, printer: PrintWriter, jisyoLike: T)(implicit jisyo: StaticJisyo[T]) {
  import Request._

  private val convert: String => Option[String] = jisyo.convert(jisyoLike) _
  private val complete: String => Option[String] = jisyo.complete(jisyoLike) _
  private val send: String => Unit = (str) => { printer.print(str); printer.flush() }

  def start(): Unit =
    requestsFrom(source) takeWhile (_ != Close) collect {
      case Convert(midashi)  => convert(midashi) map (res => s"1/$res/\n") getOrElse "4\n"
      case Complete(midashi) => complete(midashi) map (res => s"1/$res/\n") getOrElse "4\n"
      case Version           => s"${BuildInfo.name}.${BuildInfo.version} "
      case Hostname          => "" // 未実装
    } foreach send
}

final object Server {
  private def run[T](serverSocket: ServerSocket, jisyoLike: T)(implicit jisyo: StaticJisyo[T]): Try[Unit] = {
    Using.Manager { use =>
      val socket = use(serverSocket.accept())
      val inputStream = use(socket.getInputStream())
      val source = use(Source.fromInputStream(inputStream, "EUC-JP"))
      val outputStream = use(new OutputStreamWriter(socket.getOutputStream, "EUC-JP"))
      val printer = use(new PrintWriter(outputStream))

      Server(source, printer, jisyoLike).start()
    }
  } flatMap (_ => run(serverSocket, jisyoLike))

  def run[T](port: Int, jisyoLike: T)(implicit jisyo: StaticJisyo[T]): Try[Unit] =
    Using(new ServerSocket(port))(run(_, jisyoLike))
}

final object Request {
  sealed trait Status
  final case object Close extends Status
  final case class Convert(midashi: String) extends Status
  final case object Version extends Status
  final case object Hostname extends Status
  final case class Complete(midashi: String) extends Status
  final case class Invalid(number: Char) extends Status

  def requestsFrom(source: Source): Iterator[Status] =
    new Iterator[Status] {
      def hasNext: Boolean = source.hasNext

      private def nextUntilWhitespace: String =
        source.next match {
          case ' ' => ""
          case ch  => ch +: nextUntilWhitespace
        }

      def next(): Status =
        source.next match {
          case '0'   => Close
          case '1'   => Convert(nextUntilWhitespace)
          case '2'   => Version
          case '3'   => Hostname
          case '4'   => Complete(nextUntilWhitespace)
          case other => Invalid(other)
        }
    }
}
