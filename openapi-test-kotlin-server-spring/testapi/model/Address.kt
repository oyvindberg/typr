package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.Optional

data class Address(
  @JsonProperty("city") val city: Optional<String>,
  @JsonProperty("country") val country: Optional<String>,
  @JsonProperty("street") val street: Optional<String>,
  @JsonProperty("zipCode") val zipCode: Optional<String>
)