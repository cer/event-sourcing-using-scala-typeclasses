package net.chrisrichardson.eventstore.examples.scalatypeclass.banking

import net.chrisrichardson.eventstore.examples.scalatypeclass.common.Money
import org.scalacheck.{Arbitrary, Gen}

object MoneyGen {

  def toMoney(amount : Gen[Int]) = amount.map(x => Money(BigDecimal(x) / 100))

  def amountsNotGreaterThan(limit: Money): Gen[Money] =
    toMoney(Gen.chooseNum(0, toCents(limit)))

  implicit val amounts : Arbitrary[Money] =
    Arbitrary(toMoney(Gen.chooseNum(0, 100000)))

  def toCents(limit: Money): Int = (limit.amount * 100).toInt

  def amountsGreaterThan(limit: Money) =
    toMoney(Gen.chooseNum(toCents(limit) + 1, toCents(limit) + 100000))


}
