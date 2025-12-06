package testapi.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

/** Email address wrapper */
data class Email @JsonCreator constructor(@get:JsonValue val value: String) {
  override fun toString(): String {
    return value.toString()
  }

  companion object {
    /** Converts a String to this type for Spring @PathVariable binding */
    fun valueOf(s: String): Email = Email(s)
  }
}