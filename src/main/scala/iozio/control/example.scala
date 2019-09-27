package iozio.control

import java.util.concurrent.Executors

import cats.effect.ContextShift
import iozio.measure.measure
import zio.DefaultRuntime

import scala.concurrent.ExecutionContext

object io_example extends App {
  /*
  The consequence of using IO.shift is that we can’t reason about the program in a local way, when it comes to threading — we have to know the implementation details of the effects we use, to determine which thread pool a particular effect is going to run on.
  */
  // https://blog.softwaremill.com/thread-shifting-in-cats-effect-and-zio-9c184708067b
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
  import cats.effect.IO
  import cats.implicits._

  // timeouts, races, brackets  + typeclasses

  // fibers fork join race.
  val f1 = IO { Thread.sleep(500); 1 }
  val f2 = IO { Thread.sleep(500); println("computing something"); 1 }
  // one should operate on result
  // since everything is effect, then
  implicit val contextShift: ContextShift[IO] = IO.contextShift(ec)

  // forks heavily uses context shifts - which is a little bit dangerous.
  val program = for {
    r1 <- f1
    fiber1    <- f1.start
    fiber2    <- IO.never.start
    r2 <- fiber1.join
    r3 <- fiber2.cancel
  } yield r2 + r1

//  measure(program.unsafeRunSync())

  // eagerness - inconsistency typeclasses (Monad - Applicative)
  def neverA: IO[Unit] = IO { println("forever") } *> neverA
  def neverM: IO[Unit] = IO { println("forever") } >> neverM

  measure(neverM.unsafeRunSync())
}

object future_example extends App {
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  // control is tied to execution context. :(

  // in order to race 2 futures, that's the best you can do, and still you can't guarantee that another future is stopped:

}

object zio_example extends App with DefaultRuntime {
  import zio._

  // fibers fork join race.
  val f1 = IO { Thread.sleep(500); 1 }
  val f2 = IO { Thread.sleep(500); println("computing something"); 1 }
  // one should operate on result
  // since everything is effect, then
  val program = for {
    r1 <- f1
    fiber1    <- f1.fork          // running this stuff in parallel to it, getting fiber to control resources.
    fiber2    <- f2.forever.fork  // running this stuff in parallel to it, getting fiber to control resources.
    r2 <- fiber1.join
    _ <- fiber2.interrupt
  } yield r2 + r1

  measure(unsafeRun(program))
}
