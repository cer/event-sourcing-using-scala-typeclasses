package net.chrisrichardson.eventstore.examples.scalatypeclass.banking

import net.chrisrichardson.eventstore.EntityId
import net.chrisrichardson.eventstore.client.scalatypeclass.{ServiceDsl, EventStore}
import net.chrisrichardson.eventstore.examples.scalatypeclass.banking.AccountAggregateModule.AccountAggregate
import net.chrisrichardson.eventstore.examples.scalatypeclass.banking.AccountCommands.{OpenAccountCommand, DebitAccountCommand}
import net.chrisrichardson.eventstore.examples.scalatypeclass.common.Money

class AccountService(eventStore : EventStore) {

  implicit val es = eventStore

  import ServiceDsl._

  def openAccount(initialBalance : Money) = creating {
    AccountAggregate <== OpenAccountCommand(initialBalance)
  }

  def debitAccount(accountId : EntityId, amount: Money) = updating {
    AccountAggregate.withId(accountId) <== DebitAccountCommand(amount, null)
  }

}
