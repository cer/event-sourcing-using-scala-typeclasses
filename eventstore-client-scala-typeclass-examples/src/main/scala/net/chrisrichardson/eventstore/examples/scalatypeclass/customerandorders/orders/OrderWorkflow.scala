package net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.orders

import net.chrisrichardson.eventstore.client.scalatypeclass.EventConsumerDsl._
import net.chrisrichardson.eventstore.client.scalatypeclass.{EventConsumerDsl, EventStore}
import net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.orders.OrderAggregateModule.{CancelDueToInsufficientCreditCommand, MarkCreditApprovedCommand, OrderAggregate}
import net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.orders.customerevents.{CreditLimitExceededEvent, CreditReservedEvent}
import net.chrisrichardson.eventstore.subscriptions.{EventHandler, EventSubscriber}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@EventSubscriber(id = "orderWorkflow")
@Component
class OrderWorkflow @Autowired() (eventStore: EventStore) {

   implicit val es = eventStore

   @EventHandler
   val creditApproved =
     handlerForEvent[CreditReservedEvent] { de =>
       updating {
         OrderAggregate.withId(de.event.orderId) <== MarkCreditApprovedCommand
       }
     }

   @EventHandler
   val creditLimitExceededEvent =
     handlerForEvent[CreditLimitExceededEvent] { de =>
       updating {
         OrderAggregate.withId(de.event.orderId) <== CancelDueToInsufficientCreditCommand
       }
     }



 }