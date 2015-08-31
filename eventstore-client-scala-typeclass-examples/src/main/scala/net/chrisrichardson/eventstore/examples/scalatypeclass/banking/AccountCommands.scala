package net.chrisrichardson.eventstore.examples.scalatypeclass.banking

import net.chrisrichardson.eventstore.EntityId
import net.chrisrichardson.eventstore.examples.scalatypeclass.common.Money

object AccountCommands {

  sealed trait AccountCommand

  case class OpenAccountCommand(initialBalance : Money) extends AccountCommand
  case class DebitAccountCommand(amount : Money, transactionId : EntityId) extends AccountCommand
  case class CreditAccountCommand(amount : Money, transactionId : EntityId) extends AccountCommand

}
