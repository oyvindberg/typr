package testapi.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

@JsonCreator
/** ISO 4217 currency code */
case class Currency(@JsonValue value: String) extends AnyVal

object Currency {
  /** Converts a String to this type for Spring @PathVariable binding */
  def valueOf(s: String): Currency = new testapi.model.Currency(s)
}