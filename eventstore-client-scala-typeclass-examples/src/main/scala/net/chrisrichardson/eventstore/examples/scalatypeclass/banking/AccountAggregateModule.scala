package net.chrisrichardson.eventstore.examples.scalatypeclass.banking

import monocle.macros.Lenser
import net.chrisrichardson.eventstore.client.scalatypeclass.{CommandProcessingAggregate, ModifierBasedAggregate}
import net.chrisrichardson.eventstore.examples.scalatypeclass.banking.AccountCommands.{AccountCommand, CreditAccountCommand, DebitAccountCommand, OpenAccountCommand}
import net.chrisrichardson.eventstore.examples.scalatypeclass.common.Money

import scala.util.Try


object AccountAggregateModule {

  case class Account(balance : Money)

  val lenser = Lenser[Account]
  val _balance = lenser(_.balance)

  implicit object AccountAggregate extends CommandProcessingAggregate[Account]
            with ModifierBasedAggregate[Account] {

    override def modifier = {
      case AccountOpenedEvent(initialBalance) => _balance.set(initialBalance)
      case AccountDebitedEvent(amount, _) => _balance.modify(_ - amount)
      case AccountCreditedEvent(amount, _) => _balance.modify(_ + amount)
      case AccountDebitFailedDueToInsufficientFundsEvent(amount, _) => unchanged
    }

    type AggregateEvent =  AccountEvent
    type AggregateCommand =  AccountCommand

    override def processCommand(account: Account, command: AccountCommand) = Try(command match {
      case OpenAccountCommand(initialBalance) =>
        Seq(AccountOpenedEvent(initialBalance))

      case CreditAccountCommand(amount, transactionId) =>
        Seq(AccountCreditedEvent(amount, transactionId))

      case DebitAccountCommand(amount, transactionId) if amount <= account.balance =>
        Seq(AccountDebitedEvent(amount, transactionId))

      case DebitAccountCommand(amount, transactionId) =>
        Seq(AccountDebitFailedDueToInsufficientFundsEvent(amount, transactionId))
    })

    def newInstance() = Account(Money(0))

    def events(events : AccountEvent*) : Seq[AggregateEvent] = events.toSeq
  }
}
