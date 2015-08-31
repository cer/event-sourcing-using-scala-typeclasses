package net.chrisrichardson.eventstore.client.scalatypeclass

import net.chrisrichardson.eventstore.EntityEventStore.{EventIdTypeAndData, EventTypeAndData}
import net.chrisrichardson.eventstore._
import net.chrisrichardson.eventstore.eventserde.EventSerde
import net.chrisrichardson.eventstore.idgeneration.IdGenerator
import net.chrisrichardson.eventstore.subscriptions._
import net.chrisrichardson.utils.logging.Logging
import org.springframework.stereotype.Component

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.reflect.ClassTag

class EventStoreToEntityStoreAdapter(
    entityEventStore : EntityEventStore, idGenerator : IdGenerator, eventSerde : EventSerde)
      extends EventStore with EventStoreSubscriptionManagement with Logging {


  override def save[T : Aggregate : ClassTag](events: Seq[Aggregate[T]#AggregateEvent], assignedId : Option[EntityId] = None, triggeringEvent : Option[ReceiptHandle] = None) = {
    for (response <- entityEventStore.create(assignedId, entityClassName, toEventTypeAndData(events), triggeringEvent)) yield {
      EntityIdAndVersion(response.entityIdTypeAndVersion.entityId, response.entityIdTypeAndVersion.version)
    }
  }

  def entityClassName[T : Aggregate : ClassTag]: String = {
    val name = entityClass.getName
    if (name.contains("Nothing")) throw new RuntimeException("Really!! " + name)
    name
  }

  def entityClass[T : Aggregate : ClassTag]: Class[T] = implicitly[ClassTag[T]].runtimeClass.asInstanceOf[Class[T]]

  def toEventTypeAndData(events : Seq[Event]) : Seq[EventTypeAndData] = events.map(ev => toEventTypeAndData(ev))
  def toEventTypeAndData(event: Event) : EventTypeAndData = EventTypeAndData(event.getClass.getName, eventSerde.serialize(event))


  override def find[T : Aggregate : ClassTag ](entityId: EntityId) = {
    findOptional[T](entityId) map {
      case None =>
        throw new EntityNotFoundException(entityClass, entityId)
      case Some(ewm) =>
        ewm
    }
  }

  def maybeCleanupEvent(event : Event) = event match {
    case cleanupable : Cleanupable[_] => cleanupable.cleanup
    case _ => event
  }

  def toEvent(event : EventIdTypeAndData) = maybeCleanupEvent(eventSerde.deserialize(Class.forName(event.eventType), event.eventData).asInstanceOf[Event])

  override def findOptional[T : Aggregate: ClassTag](entityId: EntityId) = {
    logger.debug("loading entity: {} ", (entityClass, entityId))
    for (loadedEvents <- entityEventStore.loadEvents(EntityIdAndType(entityId, entityClassName))) yield {
      logger.info("loaded events: " + loadedEvents)
      if (loadedEvents.events.isEmpty)
        None
      else {
        val ag = implicitly[Aggregate[T]]
        val entity = ag.newInstance()
        val loadedEntity = ag.applyEvents(entity, loadedEvents.events.map{toEvent}.asInstanceOf[Seq[ag.AggregateEvent]])
        logger.info("Loaded entity: {}", loadedEntity)
        Some(EntityWithMetadata(EntityIdAndVersion(entityId, EntityVersion(loadedEvents.events.last.id)),
          loadedEntity,
          loadedEvents.triggeringEvents
        ))
      }
    }
  }


  override def update[T : Aggregate : ClassTag](entityIdAndVersion : EntityIdAndVersion, events: Seq[Aggregate[T]#AggregateEvent], triggeringEvent : Option[ReceiptHandle] = None) = {
    logger.debug("update events={}", events)
    val saveOutcome = entityEventStore.update(EntityIdAndType(entityIdAndVersion.entityId, entityClassName), entityIdAndVersion.entityVersion,
      toEventTypeAndData(events),
      triggeringEvent)
    saveOutcome onComplete { outcome => logger.info("outcome of update: " + (entityClassName, entityIdAndVersion, events, outcome))}
    for (response <- saveOutcome) yield {
      EntityIdAndVersion(entityIdAndVersion.entityId, response.entityIdTypeAndVersion.version)
    }
  }

  override def subscribe(subscriptionId: SubscriptionId): Future[AcknowledgableEventStream] =
    entityEventStore.subscribe(ConnectionId(idGenerator.nextId()), subscriptionId).
      map { case AcknowledgableSerializedEventStream(o, acknowledger) =>
      def toDispatchedEvent(se: SerializedEventWithReceiptHandle) = try {
        val ev = SubscriptionUtil.toDispatchedEvent(se, eventSerde)
        ev
      } catch {
        case t: Throwable =>
          logger.error("SubscriptionUtil.toDispatchedEvent failed", t)
          throw t
      }
      AcknowledgableEventStream(o map toDispatchedEvent, acknowledger)
    }
}
