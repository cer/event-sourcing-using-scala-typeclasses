package net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.orders.orderevents

import net.chrisrichardson.eventstore.Event
import net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.orders.OrderAggregateModule.Order
import net.chrisrichardson.eventstore.subscriptions.EventEntityUtil
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class OrderEventTest extends FlatSpec {

   it should "have correct entity type annotation" in {
     EventEntityUtil.entityClassFor(classOf[OrderEvent].asInstanceOf[Class[Event]]) shouldBe classOf[Order].getName
   }
 }
