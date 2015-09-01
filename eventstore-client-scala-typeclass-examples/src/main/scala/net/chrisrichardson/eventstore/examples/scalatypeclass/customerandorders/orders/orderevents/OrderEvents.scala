package net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.orders.orderevents

import net.chrisrichardson.eventstore.examples.scalatypeclass.common.Money
import net.chrisrichardson.eventstore.{EventEntity, EntityId, Event}

@EventEntity(entity = "net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.orders.OrderAggregateModule$Order")
trait OrderEvent extends Event

case class OrderCreatedEvent(customerId : EntityId, total : Money) extends OrderEvent
case class OrderCreditApprovedEvent() extends OrderEvent
case class OrderCancelledDueToInsufficientCreditEvent() extends OrderEvent