package skkserv.server

import buildinfo.BuildInfo
import scala.io.Source
import scala.util.{Using, Try}
import java.io.{InputStreamReader, OutputStreamWriter, PrintWriter}
import java.net.ServerSocket
import skkserv.jisyo.Jisyo

case class Server private (reader: InputStreamReader, printer: PrintWriter, jisyo: Jisyo) {
  import Request._

  private def send(str: String): Unit = { printer.print(str); printer.flush() }

  def start(): Unit =
    requestsFrom(reader) takeWhile (_ != Close) collect {
      case Convert(midashi)  => jisyo convert midashi map (res => s"1/$res/\n") getOrElse "4\n"
      case Complete(midashi) => jisyo complete midashi map (res => s"1/$res/\n") getOrElse "4\n"
      case Version           => s"${BuildInfo.name}.${BuildInfo.version} "
      case Hostname          => "" // 未実装
    } foreach send
}

final object Server {
  import skkserv.util.RichReleasable.ReleasableForComprehension

  private def run(serverSocket: ServerSocket, jisyo: Jisyo): Try[Unit] = {
    for {
      socket       <- serverSocket.accept()
      inputStream  <- socket.getInputStream()
      reader       <- new InputStreamReader(inputStream, "EUC-JP")
      outputStream <- socket.getOutputStream()
      writer       <- new OutputStreamWriter(outputStream, "EUC-JP")
      printer      <- new PrintWriter(writer)
    } yield Server(reader, printer, jisyo).start()
  } flatMap (_ => run(serverSocket, jisyo))

  def run(port: Int, jisyo: Jisyo): Try[Unit] =
    for { serverSocket <- new ServerSocket(port) } yield run(serverSocket, jisyo)
}

final object Request {
  sealed trait Status
  final case object Close extends Status
  final case class Convert(midashi: String) extends Status
  final case object Version extends Status
  final case object Hostname extends Status
  final case class Complete(midashi: String) extends Status
  final case class Invalid(number: Char) extends Status

  def requestsFrom(reader: InputStreamReader): Iterator[Status] =
    new Iterator[Status] {

      override def hasNext: Boolean = reader.ready

      private def nextUntilWhitespace: String =
        reader.read().toChar match {
          case ' ' => ""
          case ch  => ch +: nextUntilWhitespace
        }

      override def next(): Status =
        reader.read().toChar match {
          case '0'   => Close
          case '1'   => Convert(nextUntilWhitespace)
          case '2'   => Version
          case '3'   => Hostname
          case '4'   => Complete(nextUntilWhitespace)
          case other => Invalid(other)
        }
    }

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
