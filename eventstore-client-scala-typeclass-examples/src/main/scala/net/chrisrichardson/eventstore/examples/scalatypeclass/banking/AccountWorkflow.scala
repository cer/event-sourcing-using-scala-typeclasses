package net.chrisrichardson.eventstore.examples.scalatypeclass.banking

import net.chrisrichardson.eventstore.client.scalatypeclass.EventConsumerDsl._
import net.chrisrichardson.eventstore.client.scalatypeclass.{EventConsumerDsl, EventStore}
import net.chrisrichardson.eventstore.examples.scalatypeclass.banking.AccountAggregateModule.AccountAggregate
import net.chrisrichardson.eventstore.examples.scalatypeclass.banking.AccountCommands.{CreditAccountCommand, DebitAccountCommand}
import net.chrisrichardson.eventstore.subscriptions.{EventHandler, EventSubscriber}

@EventSubscriber(id = "accountEventHandlers")
class AccountWorkflow(eventStore: EventStore) {

  implicit val es = eventStore


  @EventHandler
  val performDebit =
    handlerForEvent[MoneyTransferCreatedEvent] { de =>
      updating {
        AccountAggregate.withId(de.event.details.fromAccountId) <==
          DebitAccountCommand(de.event.details.amount, de.entityId)
      }
    }

  @EventHandler
  val performCredit = handlerForEvent[DebitRecordedEvent] { de =>
    updating {
      AccountAggregate.withId(de.event.details.toAccountId) <== CreditAccountCommand(de.event.details.amount, de.entityId)
    }
  }

}