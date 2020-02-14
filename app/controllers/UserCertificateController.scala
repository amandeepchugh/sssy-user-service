package controllers

import com.google.inject.Inject
import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import models.User
import models.services.UserCertificateService
import play.api.i18n.MessagesApi

import scala.concurrent.ExecutionContext.Implicits.global


class UserCertificateController @Inject() (
                                            val messagesApi: MessagesApi,
                                            val env: Environment[User, CookieAuthenticator],
                                            userCertificateService: UserCertificateService
                                          )
  extends Silhouette[User, CookieAuthenticator] {

  def renewUserCertificate(certificateNumber: String) = SecuredAction.async { implicit request =>

    userCertificateService.isUserCertificateValidAndNeedsRenewal(certificateNumber).map {
      case true => ??? /*TODO: amandeep redirect to payment gateway */
      case false => BadRequest("Certificate is not eligible for a renewal")
    }
  }
}
