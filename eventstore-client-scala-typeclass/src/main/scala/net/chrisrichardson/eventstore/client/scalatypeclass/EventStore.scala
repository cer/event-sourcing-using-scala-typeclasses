package net.chrisrichardson.eventstore.client.scalatypeclass

import net.chrisrichardson.eventstore._
import net.chrisrichardson.eventstore.subscriptions.ReceiptHandle

import scala.concurrent.Future
import scala.reflect.ClassTag

trait EventStore {

  def save[T : Aggregate : ClassTag](events: Seq[Aggregate[T]#AggregateEvent], assignedId: Option[EntityId] = None, triggeringEvent: Option[ReceiptHandle] = None): Future[EntityIdAndVersion]

  def update[T : Aggregate : ClassTag](entityIdAndVersion: EntityIdAndVersion, events: Seq[Aggregate[T]#AggregateEvent], triggeringEvent: Option[ReceiptHandle] = None): Future[EntityIdAndVersion]

  def find[T : Aggregate : ClassTag](entityId: EntityId): Future[EntityWithMetadata[T]]

  def findOptional[T : Aggregate : ClassTag](entityId: EntityId): Future[Option[EntityWithMetadata[T]]]
}



case class EntityWithIdAndVersion[T : Aggregate](entityIdAndVersion : EntityIdAndVersion, entity : T) {
  def entityId = entityIdAndVersion.entityId
  def entityVersion = entityIdAndVersion.entityVersion
}

case class EntityWithMetadata[T : Aggregate](entityIdAndVersion : EntityIdAndVersion, entity : T, consumedEvents : Set[ReceiptHandle]) {
  def toEntityWithIdAndVersion = EntityWithIdAndVersion(entityIdAndVersion, entity)
  def entityId = entityIdAndVersion.entityId
  def entityVersion = entityIdAndVersion.entityVersion
}
