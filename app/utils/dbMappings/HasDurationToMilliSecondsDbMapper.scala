package utils.dbMappings
import scala.concurrent.duration.Duration
import slick.driver.JdbcProfile
import java.util.concurrent.TimeUnit.MILLISECONDS
trait HasDurationToMilliSecondsDbMapper {
  protected val driver: JdbcProfile
  import driver.api._
  implicit def durationToMilliseconds =
    MappedColumnType.base[Duration, Long] (
    _.toMillis, Duration(_, MILLISECONDS)
  )
}
