package testapi.api

import testapi.model.Error

/** Generic response type for shape: 200, 4XX, 5XX */
sealed trait Response2004XX5XX[T200] {
  def status: String
}

object Response2004XX5XX {
  case class Status200[T200](value: T200) extends Response2004XX5XX[T200] {
    override lazy val status: String = "200"
  }

  case class Status4XX[T200](
    /** HTTP status code */
    statusCode: Int,
    value: Error
  ) extends Response2004XX5XX[T200] {
    override lazy val status: String = "4XX"
  }

  case class Status5XX[T200](
    /** HTTP status code */
    statusCode: Int,
    value: Error
  ) extends Response2004XX5XX[T200] {
    override lazy val status: String = "5XX"
  }
}