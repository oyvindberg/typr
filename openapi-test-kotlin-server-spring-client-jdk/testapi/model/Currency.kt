package testapi.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

/** ISO 4217 currency code */
data class Currency @JsonCreator constructor(@get:JsonValue val value: String) {
  override fun toString(): String {
    return value
  }

  companion object {
    /** Converts a String to this type for Spring @PathVariable binding */
    fun valueOf(s: String): Currency = Currency(s)
  }
}