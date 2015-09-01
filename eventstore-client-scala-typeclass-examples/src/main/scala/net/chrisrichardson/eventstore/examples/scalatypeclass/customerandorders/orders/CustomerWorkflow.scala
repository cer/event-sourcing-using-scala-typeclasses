package net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.orders

import net.chrisrichardson.eventstore.client.scalatypeclass.EventConsumerDsl._
import net.chrisrichardson.eventstore.client.scalatypeclass.{EventConsumerDsl, EventStore}
import net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.customers.CustomerCreatedEvent
import net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.orders.CustomerAggregateModule.{CreateCustomerCommand, CustomerAggregate, ReserveCreditCommand}
import net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.orders.orderevents.OrderCreatedEvent
import net.chrisrichardson.eventstore.subscriptions.{EventHandler, EventSubscriber}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@EventSubscriber(id = "customerWorkflow")
@Component
class CustomerWorkflow @Autowired() (eventStore: EventStore) {

  implicit val es = eventStore

  @EventHandler
  val reserveCredit =
    handlerForEvent[OrderCreatedEvent] { de =>
      updating {
        CustomerAggregate.withId(de.event.customerId) <==
          ReserveCreditCommand(de.entityId, de.event.total)
      }
    }

  @EventHandler
  val createCustomer =
    handlerForEvent[CustomerCreatedEvent] { de =>
      creatingWithId {
        CustomerAggregate.withId(de.entityId) <==
          CreateCustomerCommand(de.event.creditLimit)
      }
    }


}