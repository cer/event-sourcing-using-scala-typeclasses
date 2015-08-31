package net.chrisrichardson.eventstore.examples.scalatypeclass.customersandorders.web.controllers

import net.chrisrichardson.eventstore.EntityId
import net.chrisrichardson.eventstore.examples.scalatypeclass.common.Money

case class CreateCustomerRequest(name : String, creditLimit : Money)

case class CreateCustomerResponse(customerId : EntityId)

case class CreateOrderRequest(total : Money)

case class CreateOrderResponse(orderId : EntityId)

case class GetOrderResponse(state : Int)

case class GetCustomerCreditLimitResponse(creditLimit: Money, availableCredit : Money)






