package models

import org.joda.time.DateTime

import scala.concurrent.duration.Duration

case class UserCertificate(
                            certificateNumber: String,
                            validFrom: DateTime,
                            validity: Duration
                          )
