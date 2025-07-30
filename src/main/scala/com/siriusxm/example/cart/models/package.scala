package com.siriusxm.example.cart

import cats.data.{Validated, ValidatedNec}
import cats.implicits.catsSyntaxTuple2Semigroupal
import com.siriusxm.example.cart.errors.HttpClientErrors.{AmountValidationError, NameValidationError, PriceValidationError, ValidationError}
import io.circe.Decoder

package object models {

  case class Name(value: String) extends AnyVal
  object Name {
    def apply(value: String): ValidatedNec[ValidationError, Name] =
      Validated.condNec(
        value.nonEmpty,
        new Name(value),
        NameValidationError(value)
      )

    def unsafeApply(value: String): Name =
      new Name(value)
  }
  case class Price(value: Double) extends AnyVal
  object Price {
    def apply(value: Double): ValidatedNec[ValidationError, Price] =
      Validated.condNec(
        value > 0,
        new Price(value),
        PriceValidationError(value)
      )

    def unsafeApply(value: Double): Price =
      new Price(value)
  }
  case class Product(name: Name, price: Price)
  object Product {
    implicit val decoder: Decoder[ValidatedNec[ValidationError, Product]] =
      Decoder.forProduct2("title", "price")(Product.apply)

    def apply(name: String, price: Double): ValidatedNec[ValidationError, Product] =
      (Name(name), Price(price)).mapN(Product.apply)

    def unsafeApply(name: String, price: Double): Product =
      new Product(Name.unsafeApply(name), Price.unsafeApply(price))
  }

  case class Amount(value: Int) extends AnyVal
  object Amount {
    def apply(value: Int): ValidatedNec[ValidationError, Amount] =
      Validated.condNec(
        value >= 25,
        new Amount(value),
        AmountValidationError(value)
      )

    def unsafeApply(value: Int): Amount = new Amount(value)
  }
  case class Item (amount: Amount, product: Product){
    def +(item: Item): Item = Item.unsafeApply(Amount.unsafeApply(this.amount.value + item.amount.value), item.product)
  }

  object Item {
    def apply(amount: Int, product: Product): ValidatedNec[ValidationError, Item] =
      for {
        amount <- Amount(amount)
      } yield Item(amount, product)

    def unsafeApply(amount: Amount, product: Product): Item =
      new Item(amount, product)
  }

  case class Cart(items: Set[Item]) {
    private def updateItems(items: Set[Item], item: Item): Set[Item] =
      items - item +
        items.find(_.product == item.product)
          .map(newItem =>
            item + newItem
          ).getOrElse(item)

    def addItem(item: Item): Cart =
      Cart(items = updateItems(items, item))

    def addItems(newItems: Set[Item]): Cart =
      Cart(items = newItems.foldLeft(items)((updates, item) => updateItems(updates, item)))

    def subtotal: Double =
      items.foldLeft[Double](0.0)((sum, item) => sum + (item.amount.value * item.product.price.value))

    def totalTax: Double =
      subtotal * Cart.taxValue
  }
  object Cart {
    val taxValue = 0.125
  }

  case class Catalog(items: Seq[Product])

  object Catalog {
    val itemsOnCatalog: Seq[String] = Seq(
      "cheerios", "cornflakes", "frosties", "shreddies", "weetabix"
    )
  }
}
