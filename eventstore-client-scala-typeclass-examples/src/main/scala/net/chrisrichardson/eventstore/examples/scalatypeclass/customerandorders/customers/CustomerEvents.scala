package net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.customers

import net.chrisrichardson.eventstore.Event
import net.chrisrichardson.eventstore.examples.scalatypeclass.common.Money

trait CustomerEvent extends Event

case class CustomerCreatedEvent(name : String, creditLimit : Money) extends CustomerEvent