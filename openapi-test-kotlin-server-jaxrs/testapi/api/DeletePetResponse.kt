package testapi.api

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import testapi.api.DeletePetResponse.Status404
import testapi.api.DeletePetResponse.StatusDefault
import testapi.model.Error

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "status")
@JsonSubTypes(value = [Type(value = Status404::class, name = "404"), Type(value = StatusDefault::class, name = "default")])
sealed interface DeletePetResponse {
  /** Pet not found */
  data class Status404(@JsonProperty("value") val value: Error) : DeletePetResponse {
    override fun status(): String = "404"
  }

  /** Unexpected error */
  data class StatusDefault(
    /** HTTP status code to return */
    @JsonProperty("statusCode") val statusCode: Int,
    @JsonProperty("value") val value: Error
  ) : DeletePetResponse {
    override fun status(): String = "default"
  }

  @JsonProperty("status")
  fun status(): String
}