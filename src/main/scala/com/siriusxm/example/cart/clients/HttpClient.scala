package com.siriusxm.example.cart.clients

import cats.effect.Async
import cats.effect.kernel.Resource
import cats.implicits.catsSyntaxApplicativeError
import com.siriusxm.example.cart.errors.HttpClientErrors.ErrorHandlers.httpClientResponseHandler
import com.siriusxm.example.cart.errors.HttpClientErrors.{ApplicationError, ClientError}
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.client.Client
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import com.siriusxm.example.cart.models.Product
import org.http4s.HttpRoutes

trait HttpClient[F[_]] {
  def getProduct(productName: String): F[Either[ApplicationError, Product]]
}

class HttpClientImpl[F[_]: Async] private(_client: Client[F], _urlBase: String) extends HttpClient[F] {
  private def runGetProduct(productName: String): F[Either[ApplicationError, Product]] =
    _client
      .get[Either[ApplicationError, Product]](s"${_urlBase}/$productName.json") { response =>
        httpClientResponseHandler[F, Product](productName, response)
      }

  override def getProduct(productName: String): F[Either[ApplicationError, Product]] =
    runGetProduct(productName)
      .adaptErr(error => ClientError(error.getMessage))
}

object HttpClientImpl {
  val baseUrl = "https://raw.githubusercontent.com/mattjanks16/shopping-cart-test-data/main"

  def createHttpClientImpl[F[_]: Async](): Resource[F, HttpClient[F]] =
    EmberClientBuilder.default[F].build.map { client =>
      new HttpClientImpl[F](client, baseUrl)
    }

  def createMockClient[F[_]: Async](routes: HttpRoutes[F], url: String): HttpClient[F] = {
    val testClient = Client.fromHttpApp(routes.orNotFound)
    new HttpClientImpl[F](testClient, url)
  }

}
