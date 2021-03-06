package utils.mail

import akka.actor.ActorSystem
import javax.inject.{Inject, Singleton}
import net.ceedubs.ficus.Ficus._
import play.api.Configuration
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.mailer._

import scala.concurrent.duration._
import scala.language.postfixOps

@Singleton
class MailService @Inject() (mailerClient: MailerClient, system: ActorSystem, conf: Configuration) {

  lazy val from: String = conf.underlying.as[String]("play.mailer.from")

  def sendEmailAsync(recipients: String*)(subject: String, bodyHtml: String, bodyText: String) = {
    system.scheduler.scheduleOnce(100 milliseconds) {
      sendEmail(recipients: _*)(subject, bodyHtml, bodyText)
    }
  }
  private def sendEmail(recipients: String*)(subject: String, bodyHtml: String, bodyText: String) =
    mailerClient.send(Email(subject, from, recipients, Some(bodyText), Some(bodyHtml)))
}