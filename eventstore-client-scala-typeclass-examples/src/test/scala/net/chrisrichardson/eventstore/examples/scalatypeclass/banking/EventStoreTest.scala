package net.chrisrichardson.eventstore.examples.scalatypeclass.banking

import net.chrisrichardson.eventstore.EntityEventStore.{EntityAndEventInfo, EventIdTypeAndData, EventTypeAndData, LoadedEvents}
import net.chrisrichardson.eventstore._
import net.chrisrichardson.eventstore.client.scalatypeclass
import net.chrisrichardson.eventstore.client.scalatypeclass.{TryToFutureConversion, EventStoreToEntityStoreAdapter}
import net.chrisrichardson.eventstore.eventserde.DefaultEventSerde
import net.chrisrichardson.eventstore.examples.scalatypeclass.banking.AccountAggregateModule.{Account, AccountAggregate}
import net.chrisrichardson.eventstore.examples.scalatypeclass.banking.AccountCommands.OpenAccountCommand
import net.chrisrichardson.eventstore.examples.scalatypeclass.common.Money
import net.chrisrichardson.eventstore.idgeneration.BoundaryFlakeIdGenerator
import net.chrisrichardson.eventstore.json.EventStoreCommonObjectMapping.EventStoreCommonObjectMapper
import net.chrisrichardson.utils.json.JSonMapper
import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

import scala.concurrent.ExecutionContext.Implicits.global
import TryToFutureConversion._

@RunWith(classOf[JUnitRunner])
class EventStoreTest extends FlatSpec with MockitoSugar {

  val idGenerator = new BoundaryFlakeIdGenerator
  val entityId = EntityId(idGenerator.nextId().asString)
  val entityVersion = EntityVersion(idGenerator.nextId())

  val account0 = AccountAggregate.newInstance()

  it should "save aggregate" in {

    val entityEventStore = mock[EntityEventStore]

    val eventStore = new EventStoreToEntityStoreAdapter(entityEventStore, idGenerator, DefaultEventSerde)

    Await.result(for (events0 <- tryToFuture(AccountAggregate.processCommand(account0, OpenAccountCommand(Money(500))));
                       events = AccountAggregate.events(events0: _ *);
                       eidads = events.map { event => EventTypeAndData(event.getClass.getName, JSonMapper.toJson(event))};
                       _ = when(entityEventStore.create(None, classOf[Account].getName, eidads, None)).thenReturn(Future.successful(EntityAndEventInfo(
                         EntityIdTypeAndVersion(entityId, classOf[Account].getName, entityVersion),
                         Seq())));
                       eidv <- eventStore.save(events)) yield {

      eidv shouldBe EntityIdAndVersion(entityId, entityVersion)

    }, 1009 milliseconds)
  }

  it should "find aggregate" in {

    val entityEventStore = mock[EntityEventStore]

    val eventStore = new EventStoreToEntityStoreAdapter(entityEventStore, idGenerator, DefaultEventSerde)

    Await.result(
      for (events <- tryToFuture(AccountAggregate.processCommand(account0, OpenAccountCommand(Money(500))));
           eidads = events.map { event => EventIdTypeAndData(idGenerator.nextId(), event.getClass.getName, JSonMapper.toJson(event))};
           _ = when(entityEventStore.loadEvents(EntityIdAndType(entityId, classOf[Account].getName))).thenReturn(Future.successful(LoadedEvents(eidads, Set(), None)));
           eidv <- eventStore.find[Account](entityId)) yield {
        eidv shouldBe scalatypeclass.EntityWithMetadata(EntityIdAndVersion(entityId, EntityVersion(eidads.last.id)), Account(Money(500)), Set())
      }, 1009 milliseconds)
  }
}
