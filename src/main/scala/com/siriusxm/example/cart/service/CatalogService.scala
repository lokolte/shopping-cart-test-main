package com.siriusxm.example.cart.service

import cats.effect.Async
import cats.implicits.{catsSyntaxOptionId, toFunctorOps, toTraverseOps}
import com.siriusxm.example.cart.client.HttpClient
import com.siriusxm.example.cart.errors.HttpClientErrors.{ClientError, EntityNotFound, ParsingError}
import com.siriusxm.example.cart.models.{Catalog, Product}
import org.slf4j.{Logger, LoggerFactory}

trait CatalogService[F[_]] {
  def getProduct(productName: String): F[Option[Product]]
  def getProducts(productsOnCatalog: Seq[String]): F[Seq[Product]]
  def getCatalog(itemsOnCatalog: Seq[String]): F[Catalog]
}

class CatalogServiceImpl[F[_]: Async] private(_httpClient: HttpClient[F]) extends CatalogService[F] {
  private val logger: Logger = LoggerFactory.getLogger(getClass)

  override def getProduct(productName: String): F[Option[Product]] =
    _httpClient.getProduct(productName).map {
        case Right(product) => product.some
        case Left(error) => error match {
          case nf: EntityNotFound =>
            logger.warn(nf.message)
            None
          case pe: ParsingError =>
            logger.error(s"There was a parse error: ${pe.message}")
            None
          case ce: ClientError =>
            logger.error(s"There was an error pulling Product $productName: ${ce.message}")
            None
        }
    }

  override def getProducts(productsOnCatalog: Seq[String]): F[Seq[Product]] =
    for {
      products <- productsOnCatalog.map(getProduct).sequence
    } yield products.flatten

  override def getCatalog(productsOnCatalog: Seq[String]): F[Catalog] = {
    for {
      products <- getProducts(productsOnCatalog)
    } yield Catalog(products)
  }
}

object CatalogServiceImpl {
  def apply[F[_]: Async](client: HttpClient[F]): CatalogServiceImpl[F] =
    new CatalogServiceImpl[F](client)
}
