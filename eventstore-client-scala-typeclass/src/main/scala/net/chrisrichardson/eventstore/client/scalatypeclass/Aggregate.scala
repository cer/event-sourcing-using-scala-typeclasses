package net.chrisrichardson.eventstore.client.scalatypeclass

import net.chrisrichardson.eventstore.{EntityId, Event}

import scala.util.Try


trait Aggregate[T] {
  def newInstance(): T

  type AggregateEvent <: Event

  def applyEvent(aggregate: T, event: AggregateEvent): T

  def applyEvents(aggregate: T, events: Seq[AggregateEvent]): T =
    events.foldLeft(aggregate)(applyEvent)
}

abstract class AggregateReferenceWithoutId[T: Aggregate] {
  def create(): Try[(T, Seq[Aggregate[T]#AggregateEvent])]
}

abstract class AggregateReferenceWithId[T: Aggregate](val entityId: EntityId) {
  def create(): Try[(T, Seq[Aggregate[T]#AggregateEvent])]
  def update(newAggregate: T): Try[(T, Seq[Aggregate[T]#AggregateEvent])]
}


// These two classes are really: AggregateReferenceWithoutId and AgggregateReferenceWithId
//    update vs. create is almost orthogonal
//    except update+AggregateReferenceWithoutId doesn't work






trait CommandProcessingAggregate[T] extends Aggregate[T] {

  type AggregateCommand

  def processCommand(aggregate: T, command: AggregateCommand): Try[Seq[AggregateEvent]]

  class NewAggregateRef {

    def <==(command: AggregateCommand) = {
      new AggregateReferenceWithoutId[T]()(CommandProcessingAggregate.this) {
        def create(): Try[(T, Seq[AggregateEvent])] = {
          val newAggregate = newInstance()
          processCommand(newAggregate, command) map { events =>
            (applyEvents(newAggregate, events), events)
          }
        }
      }
    }

  }

  def <==(command: AggregateCommand) = {
    new NewAggregateRef <== command
  }

  class ExistingAggregateRef(entityId: EntityId) {


    def <==(command: AggregateCommand) = new AggregateReferenceWithId[T](entityId)(CommandProcessingAggregate.this) {

      def create(): Try[(T, Seq[AggregateEvent])] = {
        val newAggregate = newInstance()
        processCommand(newAggregate, command) map { events =>
        (applyEvents(newAggregate, events), events)
        }
      }

      def update(existingAggregate: T): Try[(T, Seq[AggregateEvent])] = {
        for (events <-processCommand(existingAggregate, command)) yield (applyEvents(existingAggregate, events), events)
      }
    }
  }

  def withId(entityId: EntityId) = new ExistingAggregateRef(entityId)

}

trait ModifierBasedAggregate[T] extends Aggregate[T] {

  def modifier: PartialFunction[Event, T => T]

  override def applyEvent(aggregate: T, event: AggregateEvent): T = modifier(event)(aggregate)

  val unchanged: T => T = identity

}
