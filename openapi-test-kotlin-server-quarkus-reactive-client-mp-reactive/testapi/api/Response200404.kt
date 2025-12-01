package testapi.api

import com.fasterxml.jackson.annotation.JsonProperty

/** Generic response type for shape: 200, 404 */
sealed interface Response200404<T200, T404> {
  data class Status200<T200, T404>(@field:JsonProperty("value") val value: T200) : Response200404<T200, T404> {
    override fun status(): String = "200"
  }

  data class Status404<T200, T404>(@field:JsonProperty("value") val value: T404) : Response200404<T200, T404> {
    override fun status(): String = "404"
  }

  @JsonProperty("status")
  fun status(): String
}