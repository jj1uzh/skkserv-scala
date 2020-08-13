package skkserv

import buildinfo.BuildInfo
import scala.io.Source
import scala.util.{Using, Try}
import java.io.{OutputStreamWriter, PrintWriter}
import java.net.ServerSocket
import skkserv.jisyo.Jisyo

/**
  * skkserv実装
  *
  * @param source 入力
  * @param printer 出力
  * @param jisyo 辞書
  */
case class Server(source: Source, printer: PrintWriter, jisyo: Jisyo) {
  import Request._

  private def send: String => Unit = (str) => { printer.print(str); printer.flush() }

  def run(): Unit =
    requestsFrom(source) takeWhile (_ != Close) collect {
      case Convert(midashi)  => jisyo convert midashi map (res => s"1/$res/\n") getOrElse "4\n"
      case Complete(midashi) => jisyo complete midashi map (res => s"1/$res/\n") getOrElse "4\n"
      case Version           => s"${BuildInfo.name}.${BuildInfo.version} "
      case Hostname          => "" // 未実装
    } foreach send
}

final object Server {

  private def run(serverSocket: ServerSocket, jisyo: Jisyo): Try[Unit] = {
    Using.Manager { use =>
      val socket = use(serverSocket.accept())
      val inputStream = use(socket.getInputStream())
      val source = use(Source.fromInputStream(inputStream, "EUC-JP"))
      val outputStream = use(new OutputStreamWriter(socket.getOutputStream, "EUC-JP"))
      val printer = use(new PrintWriter(outputStream))

      Server(source, printer, jisyo).run()
    }
  } flatMap (_ => run(serverSocket, jisyo))

  def run(port: Int, jisyo: Jisyo): Try[Unit] =
    Using(new ServerSocket(port))(run(_, jisyo))
}

final object Request {
  sealed trait Status
  final case object Close                    extends Status
  final case class Convert(midashi: String)  extends Status
  final case object Version                  extends Status
  final case object Hostname                 extends Status
  final case class Complete(midashi: String) extends Status
  final case class Invalid(number: Char)     extends Status

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
