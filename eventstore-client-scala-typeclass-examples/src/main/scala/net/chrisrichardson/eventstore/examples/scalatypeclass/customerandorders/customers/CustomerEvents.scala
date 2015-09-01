package net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.customers

import net.chrisrichardson.eventstore.{EventEntity, Event}
import net.chrisrichardson.eventstore.examples.scalatypeclass.common.Money

@EventEntity(entity="net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.customers.CustomerAggregateModule$Customer")
trait CustomerEvent extends Event

case class CustomerCreatedEvent(name : String, creditLimit : Money) extends CustomerEvent