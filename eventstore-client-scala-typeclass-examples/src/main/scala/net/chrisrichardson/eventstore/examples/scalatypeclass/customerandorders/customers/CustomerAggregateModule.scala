package net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.customers

import net.chrisrichardson.eventstore.client.scalatypeclass.CommandProcessingAggregate
import net.chrisrichardson.eventstore.examples.scalatypeclass.common.Money

import scala.util.Try

object CustomerAggregateModule {

  case class Customer(name : String)


  trait CustomerCommand
  case class CreateCustomerCommand(name : String, creditLimit : Money) extends CustomerCommand

  implicit object CustomerAggregate extends CommandProcessingAggregate[Customer] {

    type AggregateCommand = CustomerCommand

    def newInstance() = Customer(null)

    type AggregateEvent = CustomerEvent

    def processCommand(aggregate: Customer, command: CustomerCommand) = Try(command match {
      case CreateCustomerCommand(name, creditLimit) => Seq(CustomerCreatedEvent(name, creditLimit))
    })

    def applyEvent(aggregate: Customer, event: CustomerEvent) = event match {
      case CustomerCreatedEvent(newName, _) => aggregate.copy(name=newName)
    }

  }

}
