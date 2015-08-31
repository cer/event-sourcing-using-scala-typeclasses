package net.chrisrichardson.eventstore.client.scalatypeclass

import net.chrisrichardson.eventstore._
import net.chrisrichardson.utils.logging.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.reflect.ClassTag
import scala.util.{Failure, Success}

object ServiceDsl extends Logging {

  import net.chrisrichardson.eventstore.client.scalatypeclass.TryToFutureConversion._

  def creating[T](body: => AggregateReferenceWithoutId[T])(implicit eventStore: EventStore, ag: CommandProcessingAggregate[T], classTag: ClassTag[T]): Future[EntityWithIdAndVersion[T]] = {
    for ((updatedEntity, events) <- body.create();
          result <- eventStore.save[T](events, None, None)) yield {
      toEntityWithIdAndVersion(updatedEntity, result)
    }
  }

  def creatingWithId[T](body: => AggregateReferenceWithId[T])(implicit eventStore: EventStore, ag: CommandProcessingAggregate[T], classTag: ClassTag[T]): Future[EntityWithIdAndVersion[T]] = {
    val arwid = body
    for ((updatedEntity, events) <- arwid.create();
          result <- eventStore.save[T](events, Some(arwid.entityId), None)) yield {
      toEntityWithIdAndVersion(updatedEntity, result)
    }
  }

  def updating[T](body: => AggregateReferenceWithId[T])(implicit eventStore: EventStore, ag: CommandProcessingAggregate[T], classTag: ClassTag[T]): Future[EntityWithIdAndVersion[T]] = {
    val updator = body
    for (originalAggregate <- eventStore.find[T](body.entityId);
         (updatedAggregate, events) <- updator.update(originalAggregate.entity);
         savedAggregate <- if (events.nonEmpty)
           eventStore.update[T](originalAggregate.entityIdAndVersion, events) map {
             toEntityWithIdAndVersion(updatedAggregate, _)
           }
         else
           Future.successful(originalAggregate.toEntityWithIdAndVersion))
    yield savedAggregate
  }


  def toEntityWithIdAndVersion[T: Aggregate](entity: T, eiv: EntityIdAndVersion) = EntityWithIdAndVersion(eiv, entity)

  def withOptimisticLockingRetries[T](body: => Future[T]): Future[T] = {
    val MAX_RETRIES = 3

    val p = Promise[T]()

    def tryIt(attempt: Int) {

      body onComplete {
        case Success(v) =>
          p.success(v)

        case Failure(t: OptimisticLockingException) if attempt < MAX_RETRIES =>
          tryIt(attempt + 1)

        case Failure(t) =>
          p.failure(t)
      }
    }

    tryIt(0)

    p.future
  }

}
