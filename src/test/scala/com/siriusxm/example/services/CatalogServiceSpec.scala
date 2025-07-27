package com.siriusxm.example.services

import cats.effect.IO
import com.siriusxm.example.cart.services.CatalogServiceImpl
import com.siriusxm.example.util.BaseTest
import weaver.SimpleIOSuite

object CatalogServiceSpec extends SimpleIOSuite with BaseTest {
  test("Succeed to get the catalog with mocked httpclient") {
    val catalogService = CatalogServiceImpl[IO](httpClient)

    for {
      catalog <- catalogService.getCatalog(itemsOnCatalog)
    } yield expect(catalog.items == expectedCatalog)
  }

  test("Succeed to get catalog with not found product") {
    val catalogService = CatalogServiceImpl[IO](httpClient)

    for {
      catalog <- catalogService.getCatalog(Seq("unknown"))
    } yield expect(catalog.items == Seq())
  }

  test("Succeed to get catalog with a product with an invalid field") {
    val catalogService = CatalogServiceImpl[IO](httpClient)

    for {
      catalog <- catalogService.getCatalog(Seq("parseerror"))
    } yield expect(catalog.items == Seq())
  }

  test("Succeed to get catalog with a product when there was an error on the server") {
    val catalogService = CatalogServiceImpl[IO](httpClient)

    for {
      catalog <- catalogService.getCatalog(Seq("error"))
    } yield expect(catalog.items == Seq())
  }

  test("Succeed to get catalog with not found product but another that is actually present") {
    val catalogService = CatalogServiceImpl[IO](httpClient)

    for {
      catalog <- catalogService.getCatalog(Seq("unknown", "cheerios"))
    } yield expect(catalog.items == Seq(cheerios))
  }
}
