package net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.orders

import net.chrisrichardson.eventstore.EntityId
import net.chrisrichardson.eventstore.client.scalatypeclass.CommandProcessingAggregate
import net.chrisrichardson.eventstore.examples.scalatypeclass.common.Money
import net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.orders.customerevents.{CreditLimitExceededEvent, CreditReservedEvent, CustomerCreatedEvent, CustomerEvent}

import scala.util.{Success, Failure, Try}

object CustomerAggregateModule {

  case class Customer(creditLimit: Money, creditReservations: Map[EntityId, Money])

  def availableCredit(customer: Customer): Money =
    customer.creditLimit - reservedCredit(customer)

  def reservedCredit(customer: Customer): Money =
    customer.creditReservations.values.foldLeft(Money.ZERO)(_ + _)

  class CreditLimitedExceededException extends RuntimeException

  def reserveCredit(customer : Customer, orderId : EntityId, amount : Money) : Try[Customer] =
    if (amount <= availableCredit(customer))
      Success(customer.copy(creditReservations =
        customer.creditReservations + (orderId -> amount)))
    else
      Failure(new CreditLimitedExceededException())

  trait CustomerCommand
  case class CreateCustomerCommand(creditLimit: Money) extends CustomerCommand
  case class ReserveCreditCommand(orderId: EntityId, amount: Money) extends CustomerCommand

  implicit object CustomerAggregate extends CommandProcessingAggregate[Customer] {

    type AggregateCommand = CustomerCommand
    type AggregateEvent = CustomerEvent

    def newInstance() = Customer(Money.ZERO, Map())


    def processCommand(customer: Customer, command: CustomerCommand) = command match {
      case CreateCustomerCommand(name) =>
        Success(Seq(CustomerCreatedEvent(name)))

      case ReserveCreditCommand(orderId, amount) =>
        reserveCredit(customer, orderId, amount).
          map{ _ => Seq(CreditReservedEvent(orderId, amount))}.
          recover {
          case _ : CreditLimitedExceededException => Seq(CreditLimitExceededEvent(orderId))
        }
    }

    def applyEvent(customer: Customer, event: CustomerEvent) = event match {
      case CustomerCreatedEvent(creditLimit) =>
        customer.copy(creditLimit = creditLimit)
      case CreditReservedEvent(orderId, amount) =>
        reserveCredit(customer, orderId, amount).get
      case CreditLimitExceededEvent(_) =>
        customer
    }


  }

}
