package com.siriusxm.example.util

import cats.effect.IO
import com.siriusxm.example.cart.clients.HttpClientImpl
import com.siriusxm.example.cart.errors.HttpClientErrors.{EntityNotFound, ParsingError, ResponseError, ValidationError}
import com.siriusxm.example.cart.models.{Amount, Cart, Item, Product}
import com.siriusxm.example.cart.services.CatalogServiceImpl
import org.http4s.HttpRoutes
import org.http4s.dsl.io._

trait BaseTest {
  val itemsOnCatalog: Seq[String] = Seq(
    "cheerioss", "cornflakes", "frosties", "shreddies", "weetabix"
  )

  val cheeriosJson = """{
                       |  "title": "Cheerios",
                       |  "price": 8.43
                       |}""".stripMargin
  val cornflakesJson = """{
                         |  "title": "Corn Flakes",
                         |  "price": 2.52
                         |}""".stripMargin
  val frostiesJson = """{
                       |  "title": "Frosties",
                       |  "price": 4.99
                       |}""".stripMargin
  val shreddiesJson = """{
                        |  "title": "Shreddies",
                        |  "price": 4.68
                        |}""".stripMargin
  val weetabixJson = """{
                   |  "title": "Weetabix",
                   |  "price": 9.98
                   |}""".stripMargin

  val testRoutes = HttpRoutes.of[IO] {

    case GET -> Root / "cheerios.json" =>
      Ok(cheeriosJson)
    case GET -> Root / "cornflakes.json" =>
      Ok(cornflakesJson)
    case GET -> Root / "frosties.json" =>
      Ok(frostiesJson)
    case GET -> Root / "shreddies.json" =>
      Ok(shreddiesJson)
    case GET -> Root / "weetabix.json" =>
      Ok(weetabixJson)
    case GET -> Root / "invalid.json" =>
      Ok("""{"title": "", "price": -9.98}""")
    case GET -> Root / "parseerror.json" =>
      Ok("""{"invalidFieldName": "Weetabix", "price": 9.98}""")
    case GET -> Root / "unknown.json" =>
      NotFound()
    case GET -> Root / "error.json" =>
      InternalServerError()
  }

  val httpClient = HttpClientImpl.createMockClient[IO](testRoutes, "")

  val cheeriosResult = Right(Product.unsafeApply("Cheerios",8.43))
  val invalidResult = Left(new ValidationError("[The name= cannot be empty], [The price=-9.98 cannot be negative]"))
  val parseErrorResult = Left(ParsingError("parseerror"))
  val notFoundResult = Left(EntityNotFound("unknown"))
  val errorResult = Left(ResponseError("Request failed with status 500"))

  val cheerios = Product.unsafeApply("Cheerios",8.43)
  val cornflakes = Product.unsafeApply("Corn Flakes",2.52)
  val frosties = Product.unsafeApply("Frosties",4.99)
  val shreddies = Product.unsafeApply("Shreddies",4.68)
  val weetabix = Product.unsafeApply("Weetabix",9.98)

  val expectedCatalog = Seq(cornflakes, frosties, shreddies, weetabix)

  val expectedState1 = Cart(Set(Item.unsafeApply(Amount.unsafeApply(1),cornflakes)))
  val expectedState2 = Cart(Set(Item.unsafeApply(Amount.unsafeApply(2),cornflakes)))
  val expectedState3 = Cart(Set(Item.unsafeApply(Amount.unsafeApply(2),cornflakes), Item.unsafeApply(Amount.unsafeApply(1),weetabix)))

  val catalogService = CatalogServiceImpl[IO](httpClient)
}
