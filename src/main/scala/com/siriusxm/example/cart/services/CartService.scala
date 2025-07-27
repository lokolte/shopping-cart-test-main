package com.siriusxm.example.cart.services

import cats.effect.{Async, Ref}
import com.siriusxm.example.cart.models.{Cart, Item}
import cats.syntax.functor._

import java.text.DecimalFormat
import scala.math.BigDecimal.RoundingMode

trait CartService[F[_]] {
  def getState: F[Cart]
  def addItem(item: Item): F[Unit]
  def addItems(items: Set[Item]): F[Unit]
  def subtotal: F[String]
  def totalTax: F[String]
  def totalWithTax: F[String]
}

class CartServiceImpl[F[_]: Async] private(_refCart: Ref[F, Cart]) extends CartService[F] {

  private def roundDouble(n: Double): String =
    (new DecimalFormat("#.00")).format(BigDecimal(n).setScale(2, RoundingMode.HALF_UP).toDouble)

  override def getState: F[Cart] = _refCart.get

  override def addItem(item: Item): F[Unit] =
    for {
      _ <- _refCart.update { cart =>
        cart.addItem(item)
      }
    } yield ()

  override def addItems(items: Set[Item]): F[Unit] =
    for {
      _ <- _refCart.update { cart =>
        cart.addItems(items)
      }
    } yield ()

  override def subtotal: F[String] =
    for {
      cart <- _refCart.get
      subtotal = cart.subtotal
    } yield roundDouble(subtotal)

  override def totalTax: F[String] =
    for {
      cart <- _refCart.get
      totalTax = cart.totalTax
    } yield roundDouble(totalTax)

  override def totalWithTax: F[String] =
    for {
      cart <- _refCart.get
      subtotal = cart.subtotal
      totalTax = cart.totalTax
    } yield roundDouble(subtotal + totalTax)
}

object CartServiceImpl {
  def apply[F[_]: Async](refCart: Ref[F, Cart]): CartServiceImpl[F] =
    new CartServiceImpl[F](refCart)
}
