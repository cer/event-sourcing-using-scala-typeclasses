package net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.orders

import net.chrisrichardson.eventstore.EntityId
import net.chrisrichardson.eventstore.examples.scalatypeclass.common.Money
import net.chrisrichardson.eventstore.examples.scalatypeclass.customerandorders.orders.CustomerAggregateModule._
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.Matchers._

import scala.util.Failure

@RunWith(classOf[JUnitRunner])
class CustomerTest extends FlatSpec {


   val creditLimit = Money(10)

  "Empty Customer" should "calculate available credit" in {

    val customer = Customer(creditLimit, Map())

    availableCredit(customer) shouldBe creditLimit
  }

  "Non-empty Customer" should "calculate available credit" in {
    availableCredit(Customer(creditLimit, Map(EntityId("x") -> Money(4)))) shouldBe Money(6)
    availableCredit(Customer(creditLimit, Map(EntityId("x") -> Money(5), EntityId("y") -> Money(3)))) shouldBe Money(2)
  }

  "Customer" should "reserve credit when less than available" in {
    val outcome = reserveCredit(Customer(creditLimit, Map()), EntityId("x"), Money(6))
    outcome.isSuccess shouldBe true
    availableCredit(outcome.get) shouldBe Money(4)
  }

  "Customer" should "reserve credit when creditLimit" in {
    val outcome = reserveCredit(Customer(creditLimit, Map()), EntityId("x"), creditLimit)
    outcome.isSuccess shouldBe true
    availableCredit(outcome.get) shouldBe Money(0)
  }

  "Customer" should "refuse credit when creditLimit exceeded" in {
    val outcome = reserveCredit(Customer(creditLimit, Map()), EntityId("x"), Money(11))
    outcome.isFailure shouldBe true
    outcome.recover(PartialFunction(identity)).get shouldBe a[CreditLimitedExceededException]
  }

}
