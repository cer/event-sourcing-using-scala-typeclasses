package net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.orders.customerevents

import net.chrisrichardson.eventstore.Event
import net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.orders.CustomerAggregateModule.Customer
import net.chrisrichardson.eventstore.subscriptions.EventEntityUtil
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.Matchers._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CustomerEventTest extends FlatSpec {

   it should "have correct entity type annotation" in {
     EventEntityUtil.entityClassFor(classOf[CustomerEvent].asInstanceOf[Class[Event]]) shouldBe classOf[Customer].getName
   }
 }
