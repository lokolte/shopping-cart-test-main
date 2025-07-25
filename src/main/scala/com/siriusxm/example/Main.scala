package com.siriusxm.example

import cats.effect.{IO, IOApp, Ref}
import cats.implicits.toTraverseOps
import com.siriusxm.example.cart.models.{Cart, Catalog, Item}
import com.siriusxm.example.cart.client.HttpClientImpl
import com.siriusxm.example.cart.service.{CartServiceImpl, CatalogServiceImpl}

object Main extends IOApp.Simple {

  def run: IO[Unit] = {
    val httClient = new HttpClientImpl[IO]()
    val items = Catalog.itemsOnCatalog.map(httClient.getProduct).sequence

    val catalogService = CatalogServiceImpl[IO](httClient)
    val catalog = catalogService.getCatalog(Catalog.itemsOnCatalog)
    catalog.map(println)

    items.map(println).flatMap(_ => catalog.map(println))

    val cart = Cart(Set[Item]())

    for {
      refCart <- Ref.of[IO, Cart](cart)
      cartService = CartServiceImpl[IO](refCart)
      cat <- catalog
      _ <- cartService.addItem(Item(1, cat.items.head))
      _ <- cartService.getState.map(println)
      _ <- cartService.addItem(Item(1, cat.items.head))
      _ <- cartService.getState.map(println)
      weetabix = cat.items.filter(_.name == "Weetabix").head
      _ <- cartService.addItem(Item(1, weetabix))
      _ <- cartService.getState.map(println)
      subtotal <- cartService.subtotal
      _ = println(subtotal)
      totalTax <- cartService.totalTax
      _ = println(totalTax)
      totalWithTax <- cartService.totalWithTax
      _ = println(totalWithTax)
    } yield cartService

  }
}