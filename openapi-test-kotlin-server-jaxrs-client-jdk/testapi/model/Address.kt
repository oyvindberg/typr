package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Address(
  @field:JsonProperty("city") val city: String?,
  @field:JsonProperty("country") val country: String?,
  @field:JsonProperty("street") val street: String?,
  @field:JsonProperty("zipCode") val zipCode: String?
)