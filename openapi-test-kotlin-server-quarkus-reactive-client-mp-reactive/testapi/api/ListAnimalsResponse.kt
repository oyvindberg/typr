package testapi.api

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import kotlin.collections.List
import testapi.api.ListAnimalsResponse.Status200
import testapi.api.ListAnimalsResponse.Status4XX
import testapi.api.ListAnimalsResponse.Status5XX
import testapi.model.Animal
import testapi.model.Error

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "status")
@JsonSubTypes(value = [Type(value = Status200::class, name = "200"), Type(value = Status4XX::class, name = "4XX"), Type(value = Status5XX::class, name = "5XX")])
sealed interface ListAnimalsResponse {
  /** A list of animals */
  data class Status200(@JsonProperty("value") val value: List<Animal>) : ListAnimalsResponse {
    override fun status(): String = "200"
  }

  /** Client error (any 4xx status) */
  data class Status4XX(
    /** HTTP status code to return */
    @JsonProperty("statusCode") val statusCode: Int,
    @JsonProperty("value") val value: Error
  ) : ListAnimalsResponse {
    override fun status(): String = "4XX"
  }

  /** Server error (any 5xx status) */
  data class Status5XX(
    /** HTTP status code to return */
    @JsonProperty("statusCode") val statusCode: Int,
    @JsonProperty("value") val value: Error
  ) : ListAnimalsResponse {
    override fun status(): String = "5XX"
  }

  @JsonProperty("status")
  fun status(): String
}