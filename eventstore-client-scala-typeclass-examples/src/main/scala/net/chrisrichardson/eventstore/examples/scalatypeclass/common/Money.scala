package net.chrisrichardson.eventstore.examples.scalatypeclass.common

case class Money(amount : BigDecimal) extends AnyVal {
  def <=(other : Money) = amount <= other.amount
  def +(other : Money) = Money(amount + other.amount)
  def -(other : Money) = Money(amount - other.amount)
}

object Money {
  val ZERO = new Money(0)

  def apply(amount : Int) : Money = Money(BigDecimal(amount))
}
