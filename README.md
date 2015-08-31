In my [Scala By the Bay 2015 presentation](http://www.slideshare.net/chris.e.richardson/developing-functional-domain-models-with-event-sourcing-sbtb-sbtb2015)
I described a Scala typeclass-based design for Aggregates that use [Event Sourcing](https://github.com/cer/event-sourcing-examples/wiki/WhyEventSourcing).

Here is an example of a Customer aggregate.

```scala
object CustomerAggregateModule {

  case class Customer(name : String)


  trait CustomerCommand
  case class CreateCustomerCommand(name : String, creditLimit : Money) extends CustomerCommand

  implicit object CustomerAggregate extends CommandProcessingAggregate[Customer] {

    type AggregateCommand = CustomerCommand

    def newInstance() = Customer(null)

    type AggregateEvent = CustomerEvent

    def processCommand(aggregate: Customer, command: CustomerCommand) = Try(command match {
      case CreateCustomerCommand(name, creditLimit) => Seq(CustomerCreatedEvent(name, creditLimit))
    })

    def applyEvent(aggregate: Customer, event: CustomerEvent) = event match {
      case CustomerCreatedEvent(newName, _) => aggregate.copy(name=newName)
    }

  }

}

```

This repository contains both the client framework code and the Customer-Order example application from the presentation.

Note: some tests use an embedded Event Store but to test and run the service you will need [credentials for the Event Store Server](https://github.com/cer/event-sourcing-examples/wiki/AboutTheEventStoreServer).
