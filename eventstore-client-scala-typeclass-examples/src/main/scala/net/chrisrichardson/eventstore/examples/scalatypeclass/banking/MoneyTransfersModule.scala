package net.chrisrichardson.eventstore.examples.scalatypeclass.banking

import net.chrisrichardson.eventstore.EntityId
import net.chrisrichardson.eventstore.client.scalatypeclass.{CommandProcessingAggregate, Aggregate}
import net.chrisrichardson.eventstore.examples.scalatypeclass.banking.MoneyTransfersModule.MoneyTransferCommands._
import net.chrisrichardson.eventstore.examples.scalatypeclass.common.Money


import scala.util.Try

object MoneyTransfersModule {

  object TransferStates {
    sealed trait State
    object NEW extends State
    object INITIAL extends State
    object DEBITED extends State
    object COMPLETED extends State
    object FAILED_DUE_TO_INSUFFICIENT_FUNDS extends State
  }

  case class TransferDetails(fromAccountId : EntityId, toAccountId : EntityId, amount : Money)

  case class MoneyTransfer(state : TransferStates.State, details : TransferDetails)


  implicit object MoneyTransferAggregate extends Aggregate[MoneyTransfer] with CommandProcessingAggregate[MoneyTransfer] {

    type AggregateEvent = MoneyTransferEvent
    type AggregateCommand = MoneyTransferCommand

    override def newInstance() = MoneyTransfer(TransferStates.NEW, null)

    override def processCommand(aggregate: MoneyTransfer, command: MoneyTransferCommand) = Try(command match {
      case CreateMoneyTransferCommand(withDetails) if aggregate.state == TransferStates.NEW =>
        Seq(MoneyTransferCreatedEvent(withDetails))

      case RecordDebitCommand(accountId) if aggregate.state == TransferStates.INITIAL && accountId == aggregate.details.fromAccountId =>
        Seq(DebitRecordedEvent(aggregate.details))

      case RecordDebitFailedDueToInsufficientFundsCommand(accountId) if aggregate.state == TransferStates.INITIAL && accountId == aggregate.details.fromAccountId =>
        Seq(TransferFailedDueToInsufficientFundsEvent())

      case RecordCreditCommand(accountId) if aggregate.state == TransferStates.DEBITED && accountId == aggregate.details.toAccountId =>
        Seq(CreditRecordedEvent(aggregate.details))
    })

    override def applyEvent(aggregate: MoneyTransfer, event: MoneyTransferEvent): MoneyTransfer = event match {
      case MoneyTransferCreatedEvent(withDetails) => aggregate.copy(state=TransferStates.INITIAL, details=withDetails)
      case DebitRecordedEvent(_) => aggregate.copy(state=TransferStates.DEBITED)
      case TransferFailedDueToInsufficientFundsEvent() => aggregate.copy(state=TransferStates.FAILED_DUE_TO_INSUFFICIENT_FUNDS)
      case CreditRecordedEvent(_) => aggregate.copy(state=TransferStates.COMPLETED)
    }


  }

  object MoneyTransferCommands {

    sealed trait MoneyTransferCommand

    case class CreateMoneyTransferCommand(details : TransferDetails) extends MoneyTransferCommand
    case class RecordDebitCommand(accountId : EntityId) extends MoneyTransferCommand
    case class RecordDebitFailedDueToInsufficientFundsCommand(accountId : EntityId) extends MoneyTransferCommand
    case class RecordCreditCommand(accountId : EntityId) extends MoneyTransferCommand

  }


}
