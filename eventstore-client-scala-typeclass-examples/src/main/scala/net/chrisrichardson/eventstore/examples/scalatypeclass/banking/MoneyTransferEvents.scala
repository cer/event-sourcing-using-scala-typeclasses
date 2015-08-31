package net.chrisrichardson.eventstore.examples.scalatypeclass.banking

import net.chrisrichardson.eventstore.Event
import net.chrisrichardson.eventstore.examples.scalatypeclass.banking.MoneyTransfersModule.TransferDetails

trait MoneyTransferEvent extends Event
case class MoneyTransferCreatedEvent(details : TransferDetails) extends MoneyTransferEvent
case class DebitRecordedEvent(details : TransferDetails) extends MoneyTransferEvent
case class CreditRecordedEvent(details : TransferDetails) extends MoneyTransferEvent
case class TransferFailedDueToInsufficientFundsEvent() extends MoneyTransferEvent
