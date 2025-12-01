package testapi.api



/** Generic response type for shape: 200, 404 */
sealed trait Response200404[T200, T404] {
  def status: String
}

object Response200404 {
  case class Status200[T200, T404](value: T200) extends Response200404[T200, T404] {
    override lazy val status: String = "200"
  }

  case class Status404[T200, T404](value: T404) extends Response200404[T200, T404] {
    override lazy val status: String = "404"
  }
}