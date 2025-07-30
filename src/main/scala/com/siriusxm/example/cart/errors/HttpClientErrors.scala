package com.siriusxm.example.cart.errors

import cats.data.Validated.{Invalid, Valid}
import cats.data.ValidatedNec
import cats.effect.Async
import cats.implicits._
import org.http4s.Status.NotFound
import org.http4s.{EntityDecoder, Response, Status}

object HttpClientErrors {

  sealed trait ApplicationError extends Throwable {
    val message: String
  }

  sealed class ValidationError(val message: String) extends ApplicationError {
    override def toString = s"ValidationError($message)"
  }
  final case class AmountValidationError(amount: Int)
    extends ValidationError(s"The amount=$amount cannot be negative")

  final case class PriceValidationError(price: Double)
    extends ValidationError(s"The price=$price cannot be negative")

  final case class NameValidationError(name: String)
    extends ValidationError(s"The name=$name cannot be empty")

  sealed abstract class HttpError(val message: String) extends ApplicationError
  final case class EntityNotFound(name: String) extends HttpError(s"Product $name not found")
  final case class ResponseError(m: String) extends HttpError(m)

  sealed abstract class ResourceError(val message: String) extends ApplicationError
  final case class ParsingError(name: String) extends ResourceError(s"Product $name could not be parse")

  final case class ClientError(message: String) extends ApplicationError

  object ErrorHandlers {
    private def collectValidatedResult[T](
      result: ValidatedNec[ValidationError, T]
    ): Either[ApplicationError, T] =
      result match {
        case Valid(value) => Right(value)
        case Invalid(errors) =>
          Left(new ValidationError(errors.toChain.toList.map(_.message).mkString("[", "], [", "]")))
      }

    def httpClientResponseHandler[F[_]: Async, T](
      name: String,
      response: Response[F]
    )(implicit ed: EntityDecoder[F, ValidatedNec[ValidationError, T]]): F[Either[ApplicationError, T]] = response match{
      case Status.Successful(response) =>
        response.attemptAs[ValidatedNec[ValidationError, T]]
          .map(collectValidatedResult)
          .leftMap[ApplicationError](
            _ => ParsingError(name)
          ).value
          .map(_.flatten)
      case response =>
        response.status match {
          case NotFound =>
            Async[F].pure(
              Left[ApplicationError, T](EntityNotFound(name))
            )
          case _ =>
            Async[F].pure(
              Left[ApplicationError, T](ResponseError(s"Request failed with status ${response.status.code}"))
            )
        }
    }
  }
}
