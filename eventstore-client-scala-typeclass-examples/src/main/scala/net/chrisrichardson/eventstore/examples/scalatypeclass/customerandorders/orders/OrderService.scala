package net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.orders

import net.chrisrichardson.eventstore.EntityId
import net.chrisrichardson.eventstore.client.scalatypeclass.{ServiceDsl, EventStore}
import net.chrisrichardson.eventstore.examples.scalatypeclass.common.Money
import net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.orders.CustomerAggregateModule.{Customer, CustomerAggregate}
import net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.orders.OrderAggregateModule.{Order, CreateOrderCommand, OrderAggregate}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import scala.concurrent.ExecutionContext.Implicits.global

@Component
class OrderService @Autowired() (eventStore: EventStore) {

  implicit val es = eventStore

  import ServiceDsl._

  def createOrder(customerId : EntityId, total : Money) =
    eventStore.find[Customer](customerId) flatMap { customer =>
      creating {
        OrderAggregate <== CreateOrderCommand(customerId, total)
      }
    }

  def findOrder(orderId : EntityId) = eventStore.find[Order](orderId)
  def findCustomer(customerId : EntityId) = eventStore.find[Customer](customerId)

}
