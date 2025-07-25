package com.siriusxm.example

import cats.effect.IO
import cats.effect.testing.specs2.CatsEffect
import com.siriusxm.example.cart.models.Catalog
import com.siriusxm.example.cart.service.CatalogServiceImpl
import com.siriusxm.example.util.BaseTest
import org.specs2.mutable.Specification

class CatalogServiceSpec extends Specification with CatsEffect with BaseTest {
  "CatalogService" should {
    "Succeed when reaching out external repo for product data" in {
      for {
        items <- itemsOnCatalog.map(httClient.getProduct).sequence
      } yield (items == expectedCatalog)
    }

    "Succeed when mocking httpclient" in {

      val catalogService = CatalogServiceImpl[IO](httClient)
      val catalog = catalogService.getCatalog(Catalog.itemsOnCatalog)

      for {
        items <- itemsOnCatalog.map(httClient.getProduct).sequence
        _ = println(items.head.left.map(_.message))
        _ = println(expectedCatalog.head.left.map(_.message))
        _ = println(items == expectedCatalog)
      } yield (items == expectedCatalog)
    }

  }
}
