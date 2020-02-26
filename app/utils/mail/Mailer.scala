package utils.mail

import javax.inject.{Inject, Singleton}
import play.api.i18n.Messages
import play.twirl.api.Html

import scala.language.implicitConversions

@Singleton
class Mailer @Inject() (ms: MailService) {

  implicit def html2String(html: Html): String = html.toString

  def forgotPassword(email: String, link: String)(implicit m: Messages) {
    ms.sendEmailAsync(email)(
      subject = Messages("mail.forgotpwd.subject"),
      bodyHtml = views.html.mails.forgotPassword(email, link),
      bodyText = views.html.mails.forgotPasswordTxt(email, link)
    )
  }

}