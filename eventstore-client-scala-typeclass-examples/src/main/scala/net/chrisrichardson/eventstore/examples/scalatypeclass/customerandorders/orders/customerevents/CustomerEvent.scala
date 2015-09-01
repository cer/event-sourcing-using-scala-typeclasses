package net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.orders.customerevents

import net.chrisrichardson.eventstore.examples.scalatypeclass.common.Money
import net.chrisrichardson.eventstore.{EventEntity, EntityId, Event}

@EventEntity(entity = "net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.orders.CustomerAggregateModule$Customer")
trait CustomerEvent extends Event

case class CustomerCreatedEvent(creditLimit : Money) extends CustomerEvent
case class CreditReservedEvent(orderId : EntityId, amount : Money) extends CustomerEvent
case class CreditLimitExceededEvent(orderId : EntityId) extends CustomerEvent
