package io.reactors
package protocol



import scala.collection._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration._
import org.scalatest._
import org.scalatest.concurrent.AsyncTimeLimitedTests



class StreamingLibraryTest extends AsyncFunSuite with AsyncTimeLimitedTests {
  val system = ReactorSystem.default("streaming-lib")

  def timeLimit = 10.seconds

  implicit override def executionContext = ExecutionContext.Implicits.global

  test("streaming map") {
    // TODO: Implement test.
    Future(assert(true))
  }
}


object StreamingLibraryTest {
  type StreamReq[T] = Channel[Reliable.TwoWay.Req[T, Int]]

  type StreamServer[T] = Channel[StreamReq[T]]

  trait Stream[T] {
    def system: ReactorSystem

    def streamServer: StreamServer[T]

    def map[S](f: T => S)(implicit at: Arrayable[T], as: Arrayable[S]): Stream[S] =
      new Mapped(this, f)

    def foreach(f: T => Unit)(
      implicit a: Arrayable[T]
    ): Unit = {
      val medium = Backpressure.Medium.reliable[T]
      val policy = Backpressure.Policy.sliding[T](128)
      system.backpressureServer(medium, policy) { server =>
        streamServer ! server.channel
        server.connections.once onEvent { pump =>
          pump.buffer.onEvent(f)
          pump.buffer.available.filter(_ == true) on {
            while (pump.buffer.nonEmpty) pump.buffer.dequeue()
          }
        }
      }
    }
  }

  class Mapped[T, S](source: Stream[T], f: T => S)(
    implicit val at: Arrayable[T], as: Arrayable[S]
  ) extends Stream[S] {
    val system = source.system

    val streamServer: StreamServer[S] = {
      val inMedium = Backpressure.Medium.reliable[T]
      val inPolicy = Backpressure.Policy.sliding[T](128)
      val outMedium = Backpressure.Medium.reliable[S]
      val outPolicy = Backpressure.Policy.sliding[S](128)
      system.spawn(Reactor[StreamReq[S]] { self =>
        val valves = mutable.Set[Valve[S]]()

        self.main.events onEvent { backServer =>
          backServer.connectBackpressure(outMedium, outPolicy) onEvent {
            valve => valves += valve
          }
        }

        val server = self.system.channels.backpressureServer(inMedium)
          .serveGenericBackpressure(inMedium, inPolicy)
        source.streamServer ! server.channel

        server.connections.once onEvent { connection =>
          def process(): Unit = {
            connection.buffer.available.filter(_ == true).once on {
              val x = connection.buffer.dequeue()
              val y = f(x)
              valves.toEvents.map { v =>
                v.available.filter(_ == true).once.map(_ => v.channel ! y)
              }.concat onDone {
                connection.channel ! 1
                process()
              }
            }
          }
        }
      })
    }
  }

}
