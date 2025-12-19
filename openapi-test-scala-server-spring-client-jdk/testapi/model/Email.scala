package testapi.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

@JsonCreator
/** Email address wrapper */
case class Email(@JsonValue value: String) extends AnyVal

object Email {

  /** Converts a String to this type for Spring @PathVariable binding */
  def valueOf(s: String): Email = new testapi.model.Email(s)
}
