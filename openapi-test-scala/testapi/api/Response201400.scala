package testapi.api



/** Generic response type for shape: 201, 400 */
sealed trait Response201400[T201, T400] {
  def status: String
}

object Response201400 {
  case class Status201[T201, T400](value: T201) extends Response201400[T201, T400] {
    override lazy val status: String = "201"
  }

  case class Status400[T201, T400](value: T400) extends Response201400[T201, T400] {
    override lazy val status: String = "400"
  }
}