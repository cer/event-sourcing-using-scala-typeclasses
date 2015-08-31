package net.chrisrichardson.eventstore.examples.scalatypeclass.banking

import net.chrisrichardson.eventstore.EntityId
import net.chrisrichardson.eventstore.examples.scalatypeclass.banking.AccountAggregateModule._
import net.chrisrichardson.eventstore.examples.scalatypeclass.banking.MoneyGen._
import net.chrisrichardson.eventstore.examples.scalatypeclass.common.Money
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.GeneratorDrivenPropertyChecks

import scala.util.Success

@RunWith(classOf[JUnitRunner])
class AccountTest extends FlatSpec with GeneratorDrivenPropertyChecks {

  val initialBalance = Money(500)
  val newAccount = AccountAggregate.newInstance()
  val accountOpenedEvent = AccountOpenedEvent(initialBalance)
  val accountWithInitialBalance = Account(initialBalance)
  val transactionId = EntityId("Some Transaction")


  it should "process OpenAccountCommand" in {
    forAll { (initialBalance: Money) =>
      AccountAggregate.processCommand(newAccount, AccountCommands.OpenAccountCommand(initialBalance)) shouldBe Success(Seq(AccountOpenedEvent(initialBalance)))
    }
  }

  it should "apply AccountOpenedEvent" in {
    forAll { (initialBalance: Money) =>
      AccountAggregate.applyEvent(newAccount, AccountOpenedEvent(initialBalance)) shouldBe Account(initialBalance)
    }
  }

  it should "process CreditAccountCommand" in {
    forAll { (amount: Money) =>
      AccountAggregate.processCommand(accountWithInitialBalance,
        AccountCommands.CreditAccountCommand(amount, transactionId)) shouldBe Success(Seq(AccountCreditedEvent(amount, transactionId)))
    }
  }

  it should "apply AccountCreditedEvent" in {
    forAll { (amount: Money) =>
      AccountAggregate.applyEvent(accountWithInitialBalance, AccountCreditedEvent(amount, transactionId)) shouldBe Account(initialBalance + amount)
    }
  }


  it should "process DebitAccountCommand when not overdrawn" in {
    forAll (amountsNotGreaterThan(initialBalance)) { (amount: Money) =>
      AccountAggregate.processCommand(accountWithInitialBalance,
        AccountCommands.DebitAccountCommand(amount, transactionId)) shouldBe Success(Seq(AccountDebitedEvent(amount, transactionId)))
    }
  }

  it should "process DebitAccountCommand when overdrawn" in {
    forAll (amountsGreaterThan(initialBalance)) { (amount: Money) =>
      AccountAggregate.processCommand(accountWithInitialBalance,
        AccountCommands.DebitAccountCommand(amount, transactionId)) shouldBe Success(Seq(AccountDebitFailedDueToInsufficientFundsEvent(amount, transactionId)))
    }
  }

  it should "apply AccountDebitedEvent" in {
    forAll (amountsNotGreaterThan(initialBalance)) { (amount: Money) =>
      AccountAggregate.applyEvent(accountWithInitialBalance, AccountDebitedEvent(amount, transactionId)) shouldBe Account(initialBalance - amount)
    }
  }
}
