package com.siriusxm.example.cart.errors

import cats.effect.Async
import org.http4s.Status.NotFound
import org.http4s.{EntityDecoder, Response, Status}

object HttpClientErrors {

  sealed trait ApplicationError extends Throwable {
    val message: String
  }

  sealed abstract class HttpError(val message: String) extends ApplicationError
  final case class EntityNotFound(name: String) extends HttpError(s"Product $name not found")
  final case class ResponseError(m: String) extends HttpError(m)

  sealed abstract class ResourceError(val message: String) extends ApplicationError
  final case class ParsingError(name: String) extends ResourceError(s"Product $name could not be parse")

  final case class ClientError(message: String) extends ApplicationError

  object ErrorHandlers {
    def httpClientResponseHandler[F[_]: Async, T](
      name: String,
      response: Response[F]
    )(implicit ed: EntityDecoder[F, T]): F[Either[ApplicationError, T]] = response match{
      case Status.Successful(r) =>
        r.attemptAs[T]
          .leftMap[ApplicationError](
            _ => ParsingError(name)
          ).value
      case r =>
        r.status match {
          case NotFound =>
            Async[F].pure(
              Left[ApplicationError, T](EntityNotFound(name))
            )
          case _ =>
            Async[F].pure(
              Left[ApplicationError, T](ResponseError(s"Request failed with status ${r.status.code}"))
            )
        }
    }
  }
}
