package akka

import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import scalaz.{Applicative, Apply, Comonad, Contravariant, Functor, Zip}

object TypeClasses {

  object SinkInstances {
    implicit def sinkContravariant[M]: Contravariant[Sink[?, M]] =
      new Contravariant[Sink[?, M]] {
        override def contramap[A, B](r: Sink[A, M])(f: B => A): Sink[B, M] =
          r.contramap(f)
      }
  }

  object SourceInstances {
    implicit def sourceFunctor[M]: Functor[Source[?, M]] =
      new Functor[Source[?, M]] {
        override def map[A, B](fa: Source[A, M])(f: A => B): Source[B, M] =
          fa.map(f)
      }
  }

  object FlowInstances {
    import scalaz.syntax.all._

    implicit def flowContravariant[B, M]: Contravariant[Flow[?, B, M]] = new Contravariant[Flow[?, B, M]] {
      override def contramap[A, A1](r: Flow[A, B, M])(f: A1 => A): Flow[A1, B, M] =
        Flow.fromFunction(f).viaMat(r)(Keep.right)
    }

    implicit def flowFunctor[A, M]: Functor[Flow[A, ?, M]] =
      new Functor[Flow[A, ?, M]] {
        override def map[B, B1](fa: Flow[A, B, M])(f: B => B1): Flow[A, B1, M] =
          fa.map(f)
      }

//    implicit def flowZip[A, M]: Zip[Flow[A, ?, M]] = new Zip[Flow[A, ?, M]] {
//      override def zip[B1, B2](a: => Flow[A, B1, M], b: => Flow[A, B2, M]): Flow[A, (B1, B2), M] = {
//        a.zip(b)
//      }
//    }

    def liftF[A1, B1, M1, F[_]: Applicative: Comonad](f0: Flow[A1, B1, M1]): Flow[F[A1], F[B1], M1] = {
      flowContravariant.contramap(f0)(Comonad[F].copure[A1]).map(_.point)
    }
  }

}
