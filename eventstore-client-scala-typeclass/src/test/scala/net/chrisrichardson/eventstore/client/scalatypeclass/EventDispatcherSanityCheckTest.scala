package net.chrisrichardson.eventstore.client.scalatypeclass

import net.chrisrichardson.eventstore.{Event, EntityId}
import net.chrisrichardson.eventstore.subscriptions._
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner
import org.springframework.beans.factory.annotation.Autowired
import org.scalatest.Matchers._
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.util.ReflectionUtils

@RunWith(classOf[JUnitRunner])
class EventDispatcherSanityCheckTest extends FlatSpec {

  class CustomerWorkflow @Autowired()(eventStore: EventStore) {

    implicit val es = eventStore

    import ExampleDomain._
    import EventConsumerDsl._


    @EventHandler
    val reserveCredit =
      handlerForEvent[CustomerCreatedEvent] { de =>
        updating {
          CustomerAggregate.withId(de.event.someId) <==
            FooCommand()
        }
      }

  }

  it should "have one event handler" in {

    EventHandlerRegistrar.makeFromCompoundEventHandler2(SubscriberId("foo"),
      new CustomerWorkflow(null), Seq(new ScalaFutureReturningEventHandlerMethodAdapterBuilder))  should have length 1
  }

}

object ExampleDomain {

  case class Customer()

  trait CustomerCommand
  case class FooCommand() extends CustomerCommand

  trait CustomerEvent extends Event
  case class CustomerCreatedEvent(someId : EntityId) extends CustomerEvent

  implicit object CustomerAggregate extends CommandProcessingAggregate[Customer] {
    type AggregateCommand = CustomerCommand
    type AggregateEvent = CustomerEvent

    def processCommand(aggregate: Customer, command: CustomerAggregate.AggregateCommand) = ???

    def applyEvent(aggregate: Customer, event: AggregateEvent) = ???

    def newInstance() = Customer()

  }

}