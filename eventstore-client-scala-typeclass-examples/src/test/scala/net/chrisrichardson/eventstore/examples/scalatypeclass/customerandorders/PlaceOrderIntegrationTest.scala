package net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders

import net.chrisrichardson.eventstore.client.scalatypeclass.EventStore
import net.chrisrichardson.eventstore.examples.scalatypeclass.common.Money
import net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.customers.CustomerService
import net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.orders.CustomerAggregateModule.Customer
import net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.orders.CustomerAggregateModule.availableCredit
import net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.orders.OrderAggregateModule.{OrderState, Order}
import net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.orders.OrderService
import org.junit.Test
import org.junit.runner.RunWith
import org.scalatest.time.{Span, Millis}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.{IntegrationTest, SpringApplicationConfiguration}
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import org.scalatest.concurrent.Eventually._
import org.scalatest.Matchers._

@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[PlaceOrderIntegrationTestConfiguration]))
@IntegrationTest()
class PlaceOrderIntegrationTest {

  @Autowired
  var customerService: CustomerService = _

  @Autowired
  var eventStore: EventStore = _

  @Autowired
  var orderService : OrderService = _

  def await[T](body: => Future[T]) = Await.result(body, 500 milliseconds)

  val patience = PatienceConfig(Span(5000, Millis), Span(250, Millis))

  @Test
  def something: Unit = {

    val creditLimit = Money(100)

    val customer = await {
      customerService.createCustomer("MyName", creditLimit)
    }

    eventually {
      val orderCustomer = await { eventStore.find[Customer](customer.entityId) }
      orderCustomer.entity.creditLimit shouldBe creditLimit
    }(patience)

    val orderTotal = Money(90)

    val order = await { orderService.createOrder(customer.entityId, orderTotal)}

    eventually {
      val thisOrder = await { eventStore.find[Order](order.entityId) }
      thisOrder.entity.state shouldBe OrderState.Open
    }(patience)

    eventually {
      val orderCustomer = await { eventStore.find[Customer](customer.entityId) }
      availableCredit(orderCustomer.entity) shouldBe (creditLimit - orderTotal)
    }(patience)
  }
}