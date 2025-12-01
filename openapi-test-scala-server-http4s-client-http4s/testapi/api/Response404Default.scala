package testapi.api

import testapi.model.Error

/** Generic response type for shape: 404, default */
sealed trait Response404Default[T404] {
  def status: String
}

object Response404Default {
  case class Status404[T404](value: T404) extends Response404Default[T404] {
    override lazy val status: String = "404"
  }

  case class StatusDefault[T404](
    /** HTTP status code */
    statusCode: Int,
    value: Error
  ) extends Response404Default[T404] {
    override lazy val status: String = "default"
  }
}