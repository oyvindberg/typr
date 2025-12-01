package testapi.api

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.util.Optional
import java.util.UUID
import testapi.api.GetPetResponse.Status200
import testapi.api.GetPetResponse.Status404
import testapi.model.Error
import testapi.model.Pet

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "status")
@JsonSubTypes(value = [Type(value = Status200::class, name = "200"), Type(value = Status404::class, name = "404")])
sealed interface GetPetResponse {
  /** Pet found */
  data class Status200(
    @JsonProperty("value") val value: Pet,
    /** Whether the response was served from cache */
    @JsonProperty("X-Cache-Status") val xCacheStatus: Optional<String>,
    /** Unique request identifier for tracing */
    @JsonProperty("X-Request-Id") val xRequestId: UUID
  ) : GetPetResponse {
    override fun status(): String = "200"
  }

  /** Pet not found */
  data class Status404(
    @JsonProperty("value") val value: Error,
    /** Unique request identifier for tracing */
    @JsonProperty("X-Request-Id") val xRequestId: UUID
  ) : GetPetResponse {
    override fun status(): String = "404"
  }

  @JsonProperty("status")
  fun status(): String
}