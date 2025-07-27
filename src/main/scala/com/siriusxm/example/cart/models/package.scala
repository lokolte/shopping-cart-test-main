package com.siriusxm.example.cart

import io.circe.Decoder

package object models {

  case class Product(name: String, price: Double)
  object Product {
    implicit val decoder: Decoder[Product] =
      Decoder.forProduct2("title", "price")(Product.apply)
  }

  case class Item (amount: Int, product: Product)

  case class Cart(items: Set[Item]) {
    private def updateItems(items: Set[Item], item: Item): Set[Item] =
      items - item +
        items.find(_ == item)
          .map(newItem =>
            Item(newItem.amount + item.amount, newItem.product)
          ).getOrElse(item)

    def addItem(item: Item): Cart =
      Cart(items = updateItems(items, item))

    def addItems(newItems: Set[Item]): Cart =
      Cart(items = newItems.foldLeft(items)((updates, item) => updateItems(updates, item)))

    def subtotal: Double =
      items.foldLeft[Double](0.0)((sum, item) => sum + (item.amount * item.product.price))

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
