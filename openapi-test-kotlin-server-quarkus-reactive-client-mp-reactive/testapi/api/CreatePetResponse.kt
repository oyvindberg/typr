package testapi.api

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import testapi.api.CreatePetResponse.Status201
import testapi.api.CreatePetResponse.Status400
import testapi.model.Error
import testapi.model.Pet

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "status")
@JsonSubTypes(value = [Type(value = Status201::class, name = "201"), Type(value = Status400::class, name = "400")])
sealed interface CreatePetResponse {
  /** Pet created */
  data class Status201(@JsonProperty("value") val value: Pet) : CreatePetResponse {
    override fun status(): String = "201"
  }

  /** Invalid input */
  data class Status400(@JsonProperty("value") val value: Error) : CreatePetResponse {
    override fun status(): String = "400"
  }

  @JsonProperty("status")
  fun status(): String
}