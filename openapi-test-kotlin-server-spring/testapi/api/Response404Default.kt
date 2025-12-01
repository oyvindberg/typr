package testapi.api

import com.fasterxml.jackson.annotation.JsonProperty
import testapi.model.Error

/** Generic response type for shape: 404, default */
sealed interface Response404Default<T404> {
  data class Status404<T404>(@field:JsonProperty("value") val value: T404) : Response404Default<T404> {
    override fun status(): String = "404"
  }

  data class StatusDefault<T404>(
    /** HTTP status code */
    @field:JsonProperty("statusCode") val statusCode: Int,
    @field:JsonProperty("value") val value: Error
  ) : Response404Default<T404> {
    override fun status(): String = "default"
  }

  @JsonProperty("status")
  fun status(): String
}