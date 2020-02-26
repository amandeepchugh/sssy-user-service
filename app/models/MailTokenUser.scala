package models

import org.joda.time.DateTime
import java.util.UUID

import utils.mail.MailToken

import scala.collection.mutable
import scala.concurrent.Future

case class MailTokenUser(id: String, email: String, expirationTime: DateTime, isSignUp: Boolean) extends MailToken

object MailTokenUser {
  def apply(email: String, isSignUp: Boolean): MailTokenUser =
    MailTokenUser(UUID.randomUUID().toString, email, (new DateTime()).plusHours(24), isSignUp)

  val tokens: mutable.Map[String, MailTokenUser] = scala.collection.mutable.HashMap[String, MailTokenUser]()
  /*TODO: amandeep - persist it to permanent storage*/

  def findById(id: String): Future[Option[MailTokenUser]] = {
    Future.successful(tokens.get(id))
  }

  def save(token: MailTokenUser): Future[MailTokenUser] = {
    tokens += (token.id -> token)
    Future.successful(token)
  }

  def delete(id: String): Unit = {
    tokens.remove(id)
  }
}