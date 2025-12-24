package testapi.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

@JsonCreator
/** Unique pet identifier */
case class PetId(@JsonValue value: String) extends AnyVal

object PetId {

  /** Converts a String to this type for Spring @PathVariable binding */
  def valueOf(s: String): PetId = new testapi.model.PetId(s)
}
