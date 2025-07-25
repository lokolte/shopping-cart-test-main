package com.siriusxm.example.cart.client

import cats.effect.Async
import com.siriusxm.example.cart.errors.HttpClientErrors.ErrorHandlers.httpClientErrorResponseHandler
import com.siriusxm.example.cart.errors.HttpClientErrors.ApplicationError
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.client.Client
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import com.siriusxm.example.cart.models.Product

trait HttpClient[F[_]] {
  def getProduct(productName: String): F[Either[ApplicationError, Product]]
}

class HttpClientImpl[F[_]: Async] extends HttpClient[F] {
  import HttpClientImpl.baseUrl

  private def getProductRequest(productName: String, client: Client[F]): F[Either[ApplicationError, Product]] =
    client
      .get[Either[ApplicationError, Product]](s"$baseUrl/$productName.json") { response =>
        httpClientErrorResponseHandler[F, Product](productName, response)
      }

  override def getProduct(productName: String): F[Either[ApplicationError, Product]] =
    EmberClientBuilder
      .default[F]
      .build
      .use(client => getProductRequest(productName, client))
}

object HttpClientImpl {
  val baseUrl = "https://raw.githubusercontent.com/mattjanks16/shopping-cart-test-data/main"
}
