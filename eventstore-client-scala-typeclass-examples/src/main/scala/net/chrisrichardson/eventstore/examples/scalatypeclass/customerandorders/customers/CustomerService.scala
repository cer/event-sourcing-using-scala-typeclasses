package net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.customers

import net.chrisrichardson.eventstore.client.scalatypeclass.{ServiceDsl, EventStore}
import net.chrisrichardson.eventstore.examples.scalatypeclass.common.Money
import net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.customers.CustomerAggregateModule.{CreateCustomerCommand, CustomerAggregate}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class CustomerService @Autowired() (eventStore: EventStore) {

  implicit val es = eventStore

  import ServiceDsl._

  def createCustomer(name : String, creditLimit : Money) =
      creating {
        CustomerAggregate <== CreateCustomerCommand(name, creditLimit)
      }

}
