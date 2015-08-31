package net.chrisrichardson.eventstore.examples.scalatypeclass.banking

import net.chrisrichardson.eventstore.client.scalatypeclass.EventStore
import net.chrisrichardson.eventstore.examples.scalatypeclass.banking.AccountAggregateModule.{Account, AccountAggregate}
import net.chrisrichardson.eventstore.examples.scalatypeclass.common.Money
import net.chrisrichardson.eventstore.idgeneration.Int128
import net.chrisrichardson.eventstore.{EntityId, EntityIdAndVersion, EntityVersion}
import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.scalatest.Matchers._
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, FlatSpec}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

@RunWith(classOf[JUnitRunner])
class AccountServiceTest extends FlatSpec with MockitoSugar with BeforeAndAfter {

  val eventStore = mock[EventStore]

  before {
    reset(eventStore)
  }

  val service = new AccountService(eventStore)

  val initialBalance = Money(501)

  it should "create an account" in {

    val events = AccountAggregate.events(AccountOpenedEvent(initialBalance))

    val fromAccountId = EntityId("fromAccount")
    val fromAccountVersion = EntityVersion(Int128.fromString("1-1"))

    val updatedFromAccountEntityIdAndVersion = EntityIdAndVersion(fromAccountId, fromAccountVersion)

    when(eventStore.save[Account](events, None, None)).thenReturn(
      Future.successful(updatedFromAccountEntityIdAndVersion)
    )

    val newAccount = Await.result(service.openAccount(initialBalance), 1 second)

    newAccount.entityIdAndVersion shouldBe updatedFromAccountEntityIdAndVersion
    newAccount.entity shouldBe Account(initialBalance)

    verify(eventStore).save[Account](events, None, None)
  }
}
