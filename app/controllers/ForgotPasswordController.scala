package controllers

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import javax.inject.Inject
import models.services.UserService
import models.{MailTokenUser, User}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{Messages, MessagesApi}
import utils.mail.{MailTokenService, Mailer}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future


class ForgotPasswordController @Inject() (
                                        val messagesApi: MessagesApi,
                                        val env: Environment[User, CookieAuthenticator],
                                        userService: UserService,
                                        tokenService: MailTokenService[MailTokenUser],
                                        mailer: Mailer)
  extends Silhouette[User, CookieAuthenticator] {

  val emailForm: Form[String] = Form(single("email" -> email))
  /**
   * Starts the reset password mechanism if the user has forgotten his password. It shows a form to insert his email address.
   */
  def view = UserAwareAction { implicit request =>
    request.identity match {
      case Some(_) => Redirect(routes.ApplicationController index())
      case None => Ok(views.html.forgotPassword(emailForm))
    }
  }

  /**
   * Sends an email to the user with a link to reset the password
   */
  def submit = UserAwareAction.async { implicit request =>
    emailForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(views.html.forgotPassword(formWithErrors))),
      email => userService.retrieve(LoginInfo(CredentialsProvider.ID, email)).flatMap {
        case Some(_) => {
          val token = MailTokenUser(email, isSignUp = false)
          tokenService.create(token).map { _ =>
            mailer.forgotPassword(email, link = routes.ResetPasswordController.view(token.id).absoluteURL())
            Ok(views.html.forgotPasswordSent(email))
          }
        }
        case None => Future.successful(BadRequest(views.html.forgotPassword(emailForm.withError("email", Messages("auth.user.notexists")))))
      }
    )
  }

}

