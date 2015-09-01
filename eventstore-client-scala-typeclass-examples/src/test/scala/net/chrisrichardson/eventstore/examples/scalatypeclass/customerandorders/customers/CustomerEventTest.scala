package net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.customers

import net.chrisrichardson.eventstore.Event
import net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.customers.CustomerAggregateModule.Customer
import net.chrisrichardson.eventstore.subscriptions.EventEntityUtil
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.Matchers._

@RunWith(classOf[JUnitRunner])
class CustomerEventTest extends FlatSpec {

  it should "have correct entity type annotation" in {
    EventEntityUtil.entityClassFor(classOf[CustomerEvent].asInstanceOf[Class[Event]]) shouldBe classOf[Customer].getName
  }
}
