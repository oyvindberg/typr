package testapi.api

import com.fasterxml.jackson.annotation.JsonProperty
import testapi.model.Error

/** Generic response type for shape: 200, 4XX, 5XX */
sealed interface Response2004XX5XX<T200> {
  data class Status200<T200>(@field:JsonProperty("value") val value: T200) : Response2004XX5XX<T200> {
    override fun status(): String = "200"
  }

  data class Status4XX<T200>(
    /** HTTP status code */
    @field:JsonProperty("statusCode") val statusCode: Int,
    @field:JsonProperty("value") val value: Error
  ) : Response2004XX5XX<T200> {
    override fun status(): String = "4XX"
  }

  data class Status5XX<T200>(
    /** HTTP status code */
    @field:JsonProperty("statusCode") val statusCode: Int,
    @field:JsonProperty("value") val value: Error
  ) : Response2004XX5XX<T200> {
    override fun status(): String = "5XX"
  }

  @JsonProperty("status")
  fun status(): String
}