package akka

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.stream.scaladsl.{ Keep, RestartSource, RunnableGraph, Sink, Source }
import scalaz.{ Functor, Monad }

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

object FlowExample extends App {
  implicit val system       = ActorSystem("MyActorSystem")
  implicit val materializer = ActorMaterializer()


  val source: Source[Int, NotUsed] = Source(1 to 10)
  val sink: Sink[Int, Future[Int]] = Sink.fold[Int, Int](0)(_ + _)

  // connect the Source to the Sink, obtaining a RunnableGraph
  val runnable: RunnableGraph[Future[Int]] = source.toMat(sink)(Keep.right)

  // materialize the flow and get the value of the FoldSink
  val sum: Future[Int] = runnable.run()

  private lazy val restartSource: (() => Source[Unit, _]) => Source[Unit, NotUsed] =
    RestartSource.onFailuresWithBackoff(
      1 second,
      3 second,
      0d,
      2
    )

  val stream: LazyList[Int] =
    0 #:: 1 #:: 2 #:: { throw new RuntimeException(); 3 } #:: 4 #:: LazyList.empty

  val shutdown = restartSource(
    () =>
      Source
        .fromIterator(() => stream.iterator)
        .mapAsync(1) { v =>
          Future.successful(println(v))
      }
  )

  shutdown
    .runReduce(Keep.right)
    .recover {
      case _ =>
        Done
    }

}
