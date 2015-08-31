package net.chrisrichardson.eventstore.examples.scalatypeclass.banking

import net.chrisrichardson.eventstore._
import net.chrisrichardson.eventstore.client.scalatypeclass.{EntityWithMetadata, EventStore}
import net.chrisrichardson.eventstore.examples.scalatypeclass.banking.AccountAggregateModule.{AccountAggregate, Account}
import net.chrisrichardson.eventstore.examples.scalatypeclass.banking.MoneyTransfersModule.{MoneyTransfer, TransferDetails}
import net.chrisrichardson.eventstore.examples.scalatypeclass.common.Money
import net.chrisrichardson.eventstore.idgeneration.Int128
import net.chrisrichardson.eventstore.subscriptions.{AcknowledgeableEvent, DispatchedEvent, ReceiptHandle, SubscriberId}
import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.scalatest.Matchers._
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, FlatSpec}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

@RunWith(classOf[JUnitRunner])
class AccountWorkflowTest extends FlatSpec with MockitoSugar with BeforeAndAfter {

  val moneyTransferId = EntityId("MyEntityid")
  val eventId = Int128.ZERO
  val fromAccountId = EntityId("fromAccount")
  val toAccountId = EntityId("toAccount")
  val subscriberId = SubscriberId("aSubscriber")
  val providerHandle = "SomeProviderHandle"

  val fromAccount = Account(Money(500))

  val eventStore = mock[EventStore]

  val workflow = new AccountWorkflow(eventStore)

  before {
    reset(eventStore)
  }

  val accountVersion = EntityVersion(Int128.fromString("1-1"))

  val transferAmount = Money(150)

  it should "handle MoneyTransferCreatedEvent" in {


    val event = MoneyTransferCreatedEvent(TransferDetails(fromAccountId, toAccountId, transferAmount))
    val receiptHandle = ReceiptHandle(subscriberId, EventIdAndType(eventId, event.getClass.getName),
      EntityIdAndType(moneyTransferId, classOf[MoneyTransfer].getName), providerHandle)

    // FIXME - Looks like AcknowledgeableEvent can be removed
    // FIXME - DispatchedEvent can be simplified since receiptHandle contains most information

    val ae = AcknowledgeableEvent(subscriberId, DispatchedEvent(moneyTransferId, eventId, event, receiptHandle, None))

    val fromAccountEntityIdAndVersion = EntityIdAndVersion(fromAccountId, accountVersion)
    val updatedFromAccountEntityIdAndVersion = EntityIdAndVersion(fromAccountId, accountVersion)

    when(eventStore.find[Account](fromAccountId)).
      thenReturn(Future.successful(
        EntityWithMetadata(fromAccountEntityIdAndVersion, fromAccount, Set())))

    val debitEvent = AccountDebitedEvent(transferAmount, moneyTransferId)

    when(eventStore.update[Account](fromAccountEntityIdAndVersion, AccountAggregate.events(debitEvent), Some(receiptHandle))).thenReturn(
      Future.successful(updatedFromAccountEntityIdAndVersion)
    )

    val updatedFromAccountWithIdAndVersion = Await.result(workflow.performDebit(ae), 1 second)

    updatedFromAccountWithIdAndVersion.entityId shouldBe updatedFromAccountEntityIdAndVersion.entityId
    updatedFromAccountWithIdAndVersion.entityVersion shouldBe updatedFromAccountEntityIdAndVersion.entityVersion

    verify(eventStore).find[Account](fromAccountId)
    verify(eventStore).update[Account](fromAccountEntityIdAndVersion, AccountAggregate.events(debitEvent), Some(receiptHandle))
  }

  it should "handle DebitRecordedEvent" in {


    val event = DebitRecordedEvent(TransferDetails(fromAccountId, toAccountId, transferAmount))

    val receiptHandle = ReceiptHandle(subscriberId, EventIdAndType(eventId, event.getClass.getName),
      EntityIdAndType(moneyTransferId, classOf[MoneyTransfer].getName), providerHandle)

    // FIXME - Looks like AcknowledgeableEvent can be removed
    // FIXME - DispatchedEvent can be simplified since receiptHandle contains most information

    val ae = AcknowledgeableEvent(subscriberId, DispatchedEvent(moneyTransferId, eventId, event, receiptHandle, None))

    val toAccountEntityIdAndVersion = EntityIdAndVersion(toAccountId, accountVersion)
    val updatedToAccountEntityIdAndVersion = EntityIdAndVersion(toAccountId, accountVersion)

    when(eventStore.find[Account](toAccountId)).
      thenReturn(Future.successful(
        EntityWithMetadata(toAccountEntityIdAndVersion, fromAccount, Set())))

    val debitEvent = AccountCreditedEvent(transferAmount, moneyTransferId)

    when(eventStore.update[Account](toAccountEntityIdAndVersion, AccountAggregate.events(debitEvent), Some(receiptHandle))).thenReturn(
      Future.successful(updatedToAccountEntityIdAndVersion)
    )

    val updatedFromAccountWithIdAndVersion = Await.result(workflow.performCredit(ae), 1 second)

    updatedFromAccountWithIdAndVersion.entityId shouldBe updatedToAccountEntityIdAndVersion.entityId
    updatedFromAccountWithIdAndVersion.entityVersion shouldBe updatedToAccountEntityIdAndVersion.entityVersion

    verify(eventStore).find[Account](toAccountId)
    verify(eventStore).update[Account](toAccountEntityIdAndVersion, AccountAggregate.events(debitEvent), Some(receiptHandle))
  }

}
