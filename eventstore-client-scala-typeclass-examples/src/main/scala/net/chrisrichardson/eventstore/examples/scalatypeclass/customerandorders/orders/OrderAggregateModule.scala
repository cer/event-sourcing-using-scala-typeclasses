package net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.orders

import net.chrisrichardson.eventstore.EntityId
import net.chrisrichardson.eventstore.client.scalatypeclass.CommandProcessingAggregate
import net.chrisrichardson.eventstore.examples.scalatypeclass.common.Money
import net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.orders.orderevents.{OrderCancelledDueToInsufficientCreditEvent, OrderCreditApprovedEvent, OrderCreatedEvent, OrderEvent}

import scala.util.Try


object OrderAggregateModule {

  object OrderState extends Enumeration {
    type OrderState = Value
    val Created,Open, CancelledDueToInsufficientCredit = Value
  }

  import OrderState._

  case class Order(state : OrderState, customerId : EntityId, total : Money)

  trait OrderCommand
  case class CreateOrderCommand(customerId : EntityId, total : Money) extends OrderCommand
  case object MarkCreditApprovedCommand extends OrderCommand
  case object CancelDueToInsufficientCreditCommand extends OrderCommand

  implicit object OrderAggregate extends CommandProcessingAggregate[Order] {
    type AggregateCommand = OrderCommand
    type AggregateEvent = OrderEvent
    def newInstance() = Order(null, null, Money(0))

    def processCommand(aggregate: Order, command: OrderCommand) = Try(command match {
      case CreateOrderCommand(customerId, total) => Seq(OrderCreatedEvent(customerId, total))
      case MarkCreditApprovedCommand => Seq(OrderCreditApprovedEvent())
      case CancelDueToInsufficientCreditCommand => Seq(OrderCancelledDueToInsufficientCreditEvent())
    })

    def applyEvent(aggregate: Order, event: OrderEvent) = event match {
      case OrderCreatedEvent(customerId, total) => aggregate.copy(state=Created, customerId=customerId, total=total)
      case OrderCreditApprovedEvent() => aggregate.copy(state=Open)
      case OrderCancelledDueToInsufficientCreditEvent() => aggregate.copy(state=CancelledDueToInsufficientCredit)
    }


  }
}
