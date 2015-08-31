package net.chrisrichardson.eventstore.examples.scalatypeclass.banking

import net.chrisrichardson.eventstore.examples.scalatypeclass.common.Money
import net.chrisrichardson.eventstore.{EntityId, Event}

trait AccountEvent extends Event

trait AccountChangedEvent extends AccountEvent {
  val amount : Money
  val transactionId : EntityId
}

case class AccountOpenedEvent(initialBalance : Money) extends AccountEvent

case class AccountCreditedEvent(amount : Money, transactionId : EntityId) extends AccountChangedEvent

case class AccountDebitedEvent(amount : Money, transactionId : EntityId) extends AccountChangedEvent

case class AccountDebitFailedDueToInsufficientFundsEvent(amount : Money, transactionId : EntityId) extends AccountChangedEvent
