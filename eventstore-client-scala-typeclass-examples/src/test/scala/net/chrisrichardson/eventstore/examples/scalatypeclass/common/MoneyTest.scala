package net.chrisrichardson.eventstore.examples.scalatypeclass.common

import org.junit.runner.RunWith
import org.scalacheck.Gen
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.Matchers._

@RunWith(classOf[JUnitRunner])
class MoneyTest extends FlatSpec with GeneratorDrivenPropertyChecks {

  "Money" should "compare" in {
    forAll { (lhs: BigDecimal, rhs : BigDecimal) =>
      (Money(lhs) <= Money(rhs)) shouldBe (lhs <= rhs)
    }
  }

  // Avoids arithmetic underflow

  val someNumbers =
    for (n <- Gen.choose(-100, 100)) yield BigDecimal(n)

  "Money" should "+" in {
    forAll(someNumbers, someNumbers) { (lhs: BigDecimal, rhs : BigDecimal) =>
      (Money(lhs) + Money(rhs)).amount shouldBe (lhs + rhs)
    }
  }

  "Money" should "-" in {
    forAll(someNumbers, someNumbers) { (lhs: BigDecimal, rhs : BigDecimal) =>
      (Money(lhs) - Money(rhs)).amount shouldBe (lhs - rhs)
    }
  }
}
