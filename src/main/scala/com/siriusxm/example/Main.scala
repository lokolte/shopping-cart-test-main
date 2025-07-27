package com.siriusxm.example

import cats.effect.{ExitCode, IO, IOApp, Ref}
import com.siriusxm.example.cart.models.{Cart, Catalog, Item}
import com.siriusxm.example.cart.clients.HttpClientImpl
import com.siriusxm.example.cart.services.{CartServiceImpl, CatalogServiceImpl}

object Main extends IOApp.Simple {

  def run: IO[Unit] = {
    val appResources = for {
      httpClient <- HttpClientImpl.createHttpClientImpl[IO]()
      catalogService = CatalogServiceImpl[IO](httpClient)
    } yield catalogService

    // Resource usage
    appResources.use { catalogService =>

      for {
        catalog <- catalogService.getCatalog(Catalog.itemsOnCatalog)
        items = catalog.items
        _ = items.map(println)
        refCart <- Ref.of[IO, Cart](Cart(Set[Item]()))
        cartService = CartServiceImpl[IO](refCart)

        _ <- cartService.addItem(Item(1, catalog.items.head))
        _ <- cartService.getState.map(println)
        _ <- cartService.addItem(Item(1, catalog.items.head))
        _ <- cartService.getState.map(println)
        weetabix = catalog.items.filter(_.name == "Weetabix").head
        _ <- cartService.addItem(Item(1, weetabix))

        _ <- cartService.getState.map(println)
        subtotal <- cartService.subtotal
        _ = println(subtotal)
        totalTax <- cartService.totalTax
        _ = println(totalTax)
        totalWithTax <- cartService.totalWithTax
        _ = println(totalWithTax)
      } yield ExitCode.Success

    }

  }
}