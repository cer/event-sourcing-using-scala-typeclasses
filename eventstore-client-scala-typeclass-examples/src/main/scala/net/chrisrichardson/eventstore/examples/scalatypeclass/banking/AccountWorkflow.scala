package net.chrisrichardson.eventstore.examples.scalatypeclass.banking

import net.chrisrichardson.eventstore.client.scalatypeclass.{EventStore, EventConsumerDsl}
import EventConsumerDsl._
import net.chrisrichardson.eventstore.examples.scalatypeclass.banking.AccountAggregateModule.AccountAggregate
import net.chrisrichardson.eventstore.examples.scalatypeclass.banking.AccountCommands.{CreditAccountCommand, DebitAccountCommand}
import net.chrisrichardson.eventstore.subscriptions.{CompoundEventHandler, EventHandlerMethod, EventSubscriber}

@EventSubscriber(id = "accountEventHandlers")
class AccountWorkflow(eventStore: EventStore) extends CompoundEventHandler {

  implicit val es = eventStore


  @EventHandlerMethod
  val performDebit =
    handlerForEvent[MoneyTransferCreatedEvent] { de =>
      updating {
        AccountAggregate.withId(de.event.details.fromAccountId) <==
          DebitAccountCommand(de.event.details.amount, de.entityId)
      }
    }

  @EventHandlerMethod
  val performCredit = handlerForEvent[DebitRecordedEvent] { de =>
    updating {
      AccountAggregate.withId(de.event.details.toAccountId) <== CreditAccountCommand(de.event.details.amount, de.entityId)
    }
  }

}