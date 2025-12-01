package testapi.api

import com.fasterxml.jackson.annotation.JsonProperty

/** Generic response type for shape: 201, 400 */
sealed interface Response201400<T201, T400> {
  data class Status201<T201, T400>(@field:JsonProperty("value") val value: T201) : Response201400<T201, T400> {
    override fun status(): String = "201"
  }

  data class Status400<T201, T400>(@field:JsonProperty("value") val value: T400) : Response201400<T201, T400> {
    override fun status(): String = "400"
  }

  @JsonProperty("status")
  fun status(): String
}