package controllers

import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.api.{Environment, LoginEvent, LoginInfo, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import javax.inject.Inject
import models.services.UserService
import models.{MailTokenUser, User}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{Action, RequestHeader}
import utils.mail.MailTokenService

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

class ResetPasswordController @Inject()(
                                         val messagesApi: MessagesApi,
                                         val env: Environment[User, CookieAuthenticator],
                                         tokenService: MailTokenService[MailTokenUser],
                                         userService: UserService,
                                         authInfoRepository: AuthInfoRepository,
                                         passwordHasher: PasswordHasher
                                       ) extends Silhouette[User, CookieAuthenticator] {


  val passwordValidation = nonEmptyText(minLength = 6)
  val resetPasswordForm = Form(tuple(
    "password1" -> passwordValidation,
    "password2" -> nonEmptyText
  ) verifying (Messages("auth.passwords.notequal"), passwords => passwords._2 == passwords._1))


  def notFoundDefault(implicit request: RequestHeader) = Future.successful(NotFound(views.html.errors.notFound(request)))

  /**
   * Confirms the user's link based on the token and shows him a form to reset the password
   */
  def view(tokenId: String) = Action.async { implicit request =>
    tokenService.retrieve(tokenId).flatMap {
      case Some(token) if (!token.isSignUp && !token.isExpired) => {
        Future.successful(Ok(views.html.resetPassword(tokenId, resetPasswordForm)))
      }
      case Some(_) => {
        tokenService.consume(tokenId)
        notFoundDefault
      }
      case None => notFoundDefault
    }
  }


  /**
   * Saves the new password and authenticates the user
   */
  def submit(tokenId: String) = Action.async { implicit request =>
    resetPasswordForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(views.html.resetPassword(tokenId, formWithErrors))),
      passwords => {
        tokenService.retrieve(tokenId).flatMap {
          case Some(token) if (!token.isSignUp && !token.isExpired) => {  /*TODO: amandeep - logic repeated in view method, should be refactored*/
            val loginInfo: LoginInfo = LoginInfo(CredentialsProvider.ID, token.email)
            userService.retrieve(loginInfo).flatMap {
              case Some(user) =>
                for {
                  _ <- authInfoRepository.update(loginInfo, passwordHasher.hash(passwords._1))
                  authenticator <- env.authenticatorService.create(LoginInfo(CredentialsProvider.ID, token.email))
                  result <- env.authenticatorService.renew(authenticator, Ok(views.html.resetedPassword(user)))
                } yield {
                  tokenService.consume(tokenId)
                  env.eventBus.publish(LoginEvent(user, request, request2Messages))
                  result
                }
              case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
            }
          }
          case Some(_) => {
            tokenService.consume(tokenId)
            notFoundDefault(request)
          }
          case None => notFoundDefault
        }
      }
    )
  }


}
