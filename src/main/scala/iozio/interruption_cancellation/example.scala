package iozio.interruption_cancellation

import java.util.concurrent.Executors

import iozio.measure.measure
import zio.DefaultRuntime

import scala.concurrent.ExecutionContext

object io_example extends App {
  //todo
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  import cats.effect.IO

  List(1).length

  // fibers fork join race.
//  val f1 = IO { Thread.sleep(500); 1 }
//  val f2 = IO { Thread.sleep(500); println("computing something"); 1 }
//
//  val program = for {
//    r1     <- f1
//    fiber1 <- f1.start // running this stuff in parallel to it, getting fiber to control resources.
//    fiber2 <- f2.forever.fork // running this stuff in parallel to it, getting fiber to control resources.
//    r2     <- fiber1.join
//    _      <- fiber2.interrupt
//  } yield r2 + r1

//  measure(f1.unsafeRunSync())
//  measure(f1.unsafeRunSync())

}

object future_example extends App {
  // Not possible
}

object zio_example extends App with DefaultRuntime {
  import zio._

  // fibers fork join race.
  val f1 = IO { Thread.sleep(500); 1 }
  val f2 = IO { Thread.sleep(500); println("computing something"); 1 }

  // one should operate on result
  // since everything is effect, then
  val program = for {
    r1     <- f1
    fiber1 <- f1.fork // running this stuff in parallel to it, getting fiber to control resources.
    fiber2 <- f2.forever.fork // running this stuff in parallel to it, getting fiber to control resources.
    r2     <- fiber1.join
    _      <- fiber2.interrupt
  } yield r2 + r1

  measure(unsafeRun(program))
}
