package models.services
import org.joda.time.DateTime

import scala.concurrent.Future
import scala.concurrent.duration.Duration

class UserCertificateServiceImpl extends UserCertificateService {

  /*TODO: amandeep - implement functions*/
  override def doesUserCertificateNeedRenewal(validFrom: DateTime, validity: Duration): Boolean = true

  override def isUserCertificateValidAndNeedsRenewal(certificateNumber: String): Future[Boolean] = Future.successful(true)
}
