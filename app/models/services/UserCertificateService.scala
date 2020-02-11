package models.services

import org.joda.time.DateTime

import scala.concurrent.Future
import scala.concurrent.duration.Duration

trait UserCertificateService {

  def doesUserCertificateNeedRenewal(validFrom: DateTime, validity: Duration): Boolean
  def isUserCertificateValidAndNeedsRenewal(certificateNumber: String): Future[Boolean]
}
