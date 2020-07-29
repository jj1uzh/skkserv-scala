package skkserv

import java.net.{ServerSocket, Socket}
import scala.io.{Source, BufferedSource}
import scala.util.{Using, Try, Success, Failure}
import java.io.{InputStream, OutputStreamWriter, PrintWriter}
import Protocol.{Request}
import scala.annotation.tailrec

case class Server private (serverSocket: ServerSocket) {

  def listen(): Try[Unit] = {
    import Request._
    implicit object SourceIsReleasable extends Using.Releasable[Source] {
      def release(resource: Source): Unit = resource.close()
    }

    Using(serverSocket.accept) { socket =>
      println("connected")
      Using.Manager { use =>
        val input = use(socket.getInputStream)
        val source = use(Source.fromInputStream(input, "EUC-JP"))
        val output = use(new OutputStreamWriter(socket.getOutputStream, "EUC-JP"))
        val printer = use(new PrintWriter(output))
        def send(str: String): Unit = { printer.println(str); printer.flush() }

        Requests(source) takeWhile (_ != Close) foreach {
          case Close               => () // for exhaustive check
          case Convert(midashi)    => send("""1/実装中/""")
          case Version             => send("""skkserv-scalantv 0.1 """)
          case Hostname            => send("""127.0.0.1:0.0.0.0: """) // unimplemented
          case Abbreviate(midashi) => send("""4""")
          case Invalid(text)       => println(s"""invalid request: "${text}"""")
        }
      }
    }
  } flatMap (_ => listen())
}
