package net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.orders

import net.chrisrichardson.eventstore.client.scalatypeclass.{EventStore, EventConsumerDsl}
import EventConsumerDsl._
import net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.orders.OrderAggregateModule.{CancelDueToInsufficientCreditCommand, MarkCreditApprovedCommand, OrderAggregate}
import net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.orders.customerevents.{CreditLimitExceededEvent, CreditReservedEvent}
import net.chrisrichardson.eventstore.subscriptions.{CompoundEventHandler, EventHandlerMethod, EventSubscriber}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@EventSubscriber(id = "orderWorkflow")
@Component
class OrderWorkflow @Autowired() (eventStore: EventStore) extends CompoundEventHandler {

   implicit val es = eventStore

   @EventHandlerMethod
   val creditApproved =
     handlerForEvent[CreditReservedEvent] { de =>
       updating {
         OrderAggregate.withId(de.event.orderId) <== MarkCreditApprovedCommand
       }
     }

   @EventHandlerMethod
   val creditLimitExceededEvent =
     handlerForEvent[CreditLimitExceededEvent] { de =>
       updating {
         OrderAggregate.withId(de.event.orderId) <== CancelDueToInsufficientCreditCommand
       }
     }



 }