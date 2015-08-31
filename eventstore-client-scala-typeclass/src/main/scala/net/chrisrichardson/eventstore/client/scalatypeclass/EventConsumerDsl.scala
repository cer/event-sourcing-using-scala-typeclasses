package net.chrisrichardson.eventstore.client.scalatypeclass

import net.chrisrichardson.eventstore._
import net.chrisrichardson.eventstore.subscriptions.{AcknowledgeableEvent, DispatchedEvent}
import net.chrisrichardson.utils.logging.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.reflect.ClassTag

object EventConsumerDsl extends Logging {

  def handlerForEvent[T <: Event](body: DispatchedEvent[T] => EventStoreMutation): AcknowledgeableEvent[T] => Future[EntityWithIdAndVersion[_]] = {
    (ae: AcknowledgeableEvent[T]) =>
      body(ae.de).applyMutation(ae.asInstanceOf[AcknowledgeableEvent[Event]])
  }

  trait EventStoreMutation {

    def applyMutation(de: AcknowledgeableEvent[Event]): Future[EntityWithIdAndVersion[_]]

  }

  //  TODO - need to incorporate the triggering event

  import net.chrisrichardson.eventstore.client.scalatypeclass.TryToFutureConversion._

  def creatingWithId[T](body: => AggregateReferenceWithId[T])(implicit eventStore: EventStore, ag: CommandProcessingAggregate[T], classTag: ClassTag[T]):  EventStoreMutation =
    new EventStoreMutation {
      def applyMutation(de: AcknowledgeableEvent[Event]): Future[EntityWithIdAndVersion[_]] = {
        val arwid = body
        for ((updatedEntity, events) <- arwid.create();
             result <- eventStore.save[T](events, Some(arwid.entityId), None)) yield {
          toEntityWithIdAndVersion(updatedEntity, result)
        }
      }
    }

  def creating[T](body: => AggregateReferenceWithoutId[T])(implicit eventStore: EventStore, ag: CommandProcessingAggregate[T], classTag: ClassTag[T]):  EventStoreMutation =
    new EventStoreMutation {
      def applyMutation(de: AcknowledgeableEvent[Event]): Future[EntityWithIdAndVersion[_]] = {
        for ((updatedEntity, events) <- body.create();
             result <- eventStore.save[T](events, None, None)) yield {
          toEntityWithIdAndVersion(updatedEntity, result)
        }
      }
    }

//  TODO - need to incorporate the triggering event

  def updating[T](body: => AggregateReferenceWithId[T])(implicit eventStore: EventStore, ag: CommandProcessingAggregate[T], classTag: ClassTag[T]): EventStoreMutation =
    new EventStoreMutation {
      def applyMutation(de: AcknowledgeableEvent[Event]): Future[EntityWithIdAndVersion[_]] = {
        val updator = body
        for (originalAggregate <- eventStore.find[T](body.entityId);
             aeId = de.receiptHandle;
             (updatedAggregate, events) <- updator.update(originalAggregate.entity);
             savedAggregate <- if (events.nonEmpty)
               eventStore.update[T](originalAggregate.entityIdAndVersion, events, Some(aeId)) map {
                 toEntityWithIdAndVersion(updatedAggregate, _)
               }
             else
               Future.successful(originalAggregate.toEntityWithIdAndVersion))
        yield savedAggregate
      }
    }


  def toEntityWithIdAndVersion[T : Aggregate](entity : T, eiv: EntityIdAndVersion) =  EntityWithIdAndVersion(eiv, entity)


}