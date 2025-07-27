package com.siriusxm.example.services

import cats.effect.{IO, Ref}
import com.siriusxm.example.cart.models.{Cart, Item}
import com.siriusxm.example.cart.services.CartServiceImpl
import com.siriusxm.example.util.BaseTest
import weaver.SimpleIOSuite

object CartServiceSpec extends SimpleIOSuite with BaseTest {
  test("Succeed when all items are valid except one, purposly added a typo in the name") {
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
    } yield expect.all(
      state1 == expectedState1,
      state2 == expectedState2,
      state3 == expectedState3,
      subtotal == "15.02",
      totalTax == "1.88",
      totalWithTax == "16.90")
  }

  test("Succeed when all items are valid except one, and used the addItems should be equivalent") {
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
    } yield expect.all(
      state1 == expectedState1,
      state2 == expectedState3,
      subtotal == "15.02",
      totalTax == "1.88",
      totalWithTax == "16.90")
  }

  test("Succeed when all items are valid except one, and used the addItems should be equivalent") {
    for {
      refCart <- Ref.of[IO, Cart](Cart(Set[Item]()))
      cartService = CartServiceImpl[IO](refCart)
      state <- cartService.getState
      subtotal <- cartService.subtotal
      totalTax <- cartService.totalTax
      totalWithTax <- cartService.totalWithTax
    } yield expect.all(
      state == Cart(Set()),
      subtotal == ".00",
      totalTax == ".00",
      totalWithTax == ".00")
  }
}
