import skkserv.{Server => SKKServer}
import scala.util.{Success, Failure, Using}
import java.net.ServerSocket

object Main {
  def main(args: Array[String]): Unit = {

    Using(new ServerSocket(1178)) { serverSocket =>
      SKKServer(serverSocket).listen()
    }.flatten match {
      case Success(())        => ()
      case Failure(exception) => println(exception)
    }
    println("bye.")
  }
}
