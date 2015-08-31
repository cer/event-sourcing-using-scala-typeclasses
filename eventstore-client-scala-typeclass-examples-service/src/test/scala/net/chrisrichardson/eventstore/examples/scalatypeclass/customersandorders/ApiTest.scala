package net.chrisrichardson.eventstore.examples.scalatypeclass.customersandorders

import java.util.Collections
import net.chrisrichardson.eventstore.examples.scalatypeclass.common.Money
import net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.orders.OrderAggregateModule
import net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.orders.OrderAggregateModule.OrderState
import net.chrisrichardson.eventstore.examples.scalatypeclass.customersandorders.web.CustomersAndOrdersWebConfiguration
import net.chrisrichardson.eventstore.examples.scalatypeclass.customersandorders.web.controllers._
import net.chrisrichardson.eventstore.json.EventStoreCommonObjectMapping
import org.junit.Test
import org.junit.runner.RunWith
import org.scalatest.Matchers._
import org.scalatest.concurrent.Eventually._
import org.scalatest.time.{Millis, Span}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.autoconfigure.web.HttpMessageConverters
import org.springframework.boot.test.{SpringApplicationConfiguration, WebIntegrationTest}
import org.springframework.context.annotation.{Bean, Configuration, Import}
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.web.client.RestTemplate

@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[ApiTestConfiguration]))
@WebIntegrationTest(randomPort = true)
class ApiTest {

  @Value("${local.server.port}")
  var port: Int = _

  @Autowired
  var restTemplate: RestTemplate = _

  val patience = PatienceConfig(Span(5 * 1000, Millis), Span(200, Millis))

  val creditLimit = Money(100)
  val orderTotal = Money(80)

  @Test
  def shouldCreateCustomerAndOrder(): Unit = {
    val customerId = createCustomer()

    creditLimitAndAvailableCreditShouldBe(customerId, (creditLimit, creditLimit))

    val orderId = createOrder(customerId, orderTotal)

    orderStatusShouldBe(orderId, OrderAggregateModule.OrderState.Open)

    creditLimitAndAvailableCreditShouldBe(customerId, (creditLimit, creditLimit - orderTotal))

  }

  @Test
  def shouldRejectOrderExceedingCreditLimit(): Unit = {

    val customerId = createCustomer()

    creditLimitAndAvailableCreditShouldBe(customerId, (creditLimit, creditLimit))

    val orderId = createOrder(customerId, creditLimit + Money(1))

    orderStatusShouldBe(orderId, OrderAggregateModule.OrderState.CancelledDueToInsufficientCredit)

    creditLimitAndAvailableCreditShouldBe(customerId, (creditLimit, creditLimit))

  }

  def orderStatusShouldBe(orderId: String, expectedState: OrderState.Value) {
    eventually {
      val getOrderResponse = restTemplate.getForEntity(s"http://localhost:$port/orders/$orderId", classOf[GetOrderResponse])
      getOrderResponse.getStatusCode shouldBe HttpStatus.OK
      getOrderResponse.getBody.state shouldBe expectedState.id
    }(patience)
  }

  def createOrder(customerId: String, orderTotal : Money): String = {
    val createOrderResponse = restTemplate.postForEntity(s"http://localhost:$port/customers/$customerId/orders",
      CreateOrderRequest(orderTotal), classOf[CreateOrderResponse])

    createOrderResponse.getStatusCode shouldBe HttpStatus.OK

    val orderId: String = createOrderResponse.getBody.orderId.id
    orderId
  }

  def creditLimitAndAvailableCreditShouldBe(customerId: String, creditLimitAndAvailableCredit : (Money, Money)) {
    val (creditLimit, availableCredit) = creditLimitAndAvailableCredit
    eventually {
      val getOrderResponse = restTemplate.getForEntity(s"http://localhost:$port/customers/$customerId/creditlimit", classOf[GetCustomerCreditLimitResponse])
      getOrderResponse.getStatusCode shouldBe HttpStatus.OK
      getOrderResponse.getBody.creditLimit shouldBe creditLimit
      getOrderResponse.getBody.availableCredit shouldBe availableCredit
    }(patience)
  }

  def createCustomer(): String = {
    val createCustomerResponse = restTemplate.postForEntity(s"http://localhost:$port/customers",
      CreateCustomerRequest("Fred", creditLimit), classOf[CreateCustomerResponse])

    createCustomerResponse.getStatusCode shouldBe HttpStatus.OK
    createCustomerResponse.getBody.customerId shouldNot be(null)

    val customerId: String = createCustomerResponse.getBody.customerId.id
    customerId
  }
}

@Configuration
@Import(Array(classOf[CustomersAndOrdersWebConfiguration]))
class ApiTestConfiguration {

  @Bean
  def restTemplate(converters: HttpMessageConverters): RestTemplate = {
    // TODO - cleanup this code
    val restTemplate = new RestTemplate
    val converter = new MappingJackson2HttpMessageConverter
    converter.setObjectMapper(EventStoreCommonObjectMapping.getObjectMapper)
    val httpMessageConverters = Collections.singletonList(converter)
    restTemplate.setMessageConverters(httpMessageConverters.asInstanceOf[java.util.List[HttpMessageConverter[_]]])
    restTemplate
  }
}
