package com.siriusxm.example.util

import cats.effect.IO
import com.siriusxm.example.cart.client.HttpClientImpl
import com.siriusxm.example.cart.errors.HttpClientErrors.EntityNotFound
import com.siriusxm.example.cart.models.{Cart, Item, Product}
import com.siriusxm.example.cart.service.CatalogServiceImpl

trait BaseTest {
  val httClient = new HttpClientImpl[IO]()
  val catalogService = CatalogServiceImpl[IO](httClient)

  val itemsOnCatalog: Seq[String] = Seq(
    "cheerioss", "cornflakes", "frosties", "shreddies", "weetabix"
  )

  val expectedCatalog = Seq(
    Left(EntityNotFound("cheerioss")), Right(Product("Corn Flakes",2.52)), Right(Product("Frosties",4.99)), Right(Product("Shreddies",4.68)), Right(Product("Weetabix",9.98))
  )

  val expectedState1 = Cart(Set(Item(1,Product("Corn Flakes",2.52))))
  val expectedState2 = Cart(Set(Item(2,Product("Corn Flakes",2.52))))
  val expectedState3 = Cart(Set(Item(2,Product("Corn Flakes",2.52)), Item(1,Product("Weetabix",9.98))))

}
