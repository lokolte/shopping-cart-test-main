package com.siriusxm.example

import cats.effect.{IO, Ref}
import cats.effect.testing.specs2.CatsEffect
import com.siriusxm.example.cart.client.HttpClientImpl
import com.siriusxm.example.cart.models.{Cart, Catalog, Item}
import com.siriusxm.example.cart.service.{CartServiceImpl, CatalogServiceImpl}
import com.siriusxm.example.util.BaseTest
import org.specs2.mutable.Specification

class CartServiceSpec extends Specification with CatsEffect with BaseTest {
  "CartService" should {
    "Succeed when all items are valid except one, purposly added a typo in the name" in {
      for {
        refCart <- Ref.of[IO, Cart](Cart(Set[Item]()))
        cartService = CartServiceImpl[IO](refCart)
        catalog <- catalogService.getCatalog(itemsOnCatalog)
        _ <- cartService.addItem(Item(1, catalog.items.head))
        state1 <- cartService.getState
        _ <- cartService.addItem(Item(1, catalog.items.head))
        state2 <- cartService.getState
        weetabix = catalog.items.filter(_.name == "Weetabix").head
        _ <- cartService.addItem(Item(1, weetabix))
        state3 <- cartService.getState
        subtotal <- cartService.subtotal
        totalTax <- cartService.totalTax
        totalWithTax <- cartService.totalWithTax
      } yield ((state1 == expectedState1) &&
        (state2 == expectedState2) &&
        (state3 == expectedState3) &&
        (subtotal == "15.02") &&
        (totalTax == "1.88") &&
        (totalWithTax == "16.90"))
    }

    "Succeed when all items are valid except one, and used the addItems should be equivalent" in {
      for {
        refCart <- Ref.of[IO, Cart](Cart(Set[Item]()))
        cartService = CartServiceImpl[IO](refCart)
        catalog <- catalogService.getCatalog(itemsOnCatalog)
        weetabix = catalog.items.filter(_.name == "Weetabix").head
        _ <- cartService.addItem(Item(1, catalog.items.head))
        state1 <- cartService.getState
        _ <- cartService.addItems(Set(Item(1, catalog.items.head), Item(1, weetabix)))
        state2 <- cartService.getState
        subtotal <- cartService.subtotal
        totalTax <- cartService.totalTax
        totalWithTax <- cartService.totalWithTax
      } yield ((state1 == expectedState1) &&
        (state2 == expectedState3) &&
        (subtotal == "15.02") &&
        (totalTax == "1.88") &&
        (totalWithTax == "16.90"))
    }

    "Succeed when all items are valid except one, and used the addItems should be equivalent" in {
      for {
        refCart <- Ref.of[IO, Cart](Cart(Set[Item]()))
        cartService = CartServiceImpl[IO](refCart)
        state <- cartService.getState
        subtotal <- cartService.subtotal
        totalTax <- cartService.totalTax
        totalWithTax <- cartService.totalWithTax
      } yield ((state == Cart(Set())) &&
        (subtotal == ".00") &&
        (totalTax == ".00") &&
        (totalWithTax == ".00"))
    }
  }
}
