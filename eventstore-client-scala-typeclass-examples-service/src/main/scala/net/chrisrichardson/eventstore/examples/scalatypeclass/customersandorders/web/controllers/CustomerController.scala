package net.chrisrichardson.eventstore.examples.scalatypeclass.customersandorders.web.controllers

import net.chrisrichardson.eventstore.EntityId
import net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.customers.CustomerService
import net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.orders.{CustomerAggregateModule, OrderService}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import CustomerAggregateModule._

@RestController
class CustomerController @Autowired() (customerService : CustomerService, orderService : OrderService) {

  @RequestMapping(value=Array("/customers"), method = Array(RequestMethod.POST))
  def createCustomer(@RequestBody request : CreateCustomerRequest) : Future[CreateCustomerResponse] = {
    customerService.createCustomer(request.name, request.creditLimit) map { ewidv =>
      CreateCustomerResponse(ewidv.entityId)
    }
  }

  @RequestMapping(value=Array("/customers/{customerId}/orders"), method = Array(RequestMethod.POST))
  def createCustomer(@PathVariable  customerId : String, @RequestBody request : CreateOrderRequest) : Future[CreateOrderResponse] = {
    orderService.createOrder(EntityId(customerId), request.total) map { ewidv =>
      CreateOrderResponse(ewidv.entityId)
    }
  }

  @RequestMapping(value=Array("/orders/{orderId}"), method = Array(RequestMethod.GET))
  def getOrder(@PathVariable orderId : String) : Future[GetOrderResponse] = {
    orderService.findOrder(EntityId(orderId)) map { ewidv =>
      GetOrderResponse(ewidv.entity.state.id)
    }
  }

  @RequestMapping(value=Array("/customers/{customerId}/creditlimit"), method = Array(RequestMethod.GET))
  def getCustomer(@PathVariable customerId : String) : Future[GetCustomerCreditLimitResponse] = {
    orderService.findCustomer(EntityId(customerId)) map { ewidv =>
      GetCustomerCreditLimitResponse(ewidv.entity.creditLimit, availableCredit(ewidv.entity))
    }
  }

}
