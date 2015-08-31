package net.chrisrichardson.eventstore.client.scalatypeclass

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object TryToFutureConversion {

  implicit def tryToFuture[T](t:Try[T]):Future[T] = {
    t match{
      case Success(s) => Future.successful(s)
      case Failure(ex) => Future.failed(ex)
    }
  }

}
