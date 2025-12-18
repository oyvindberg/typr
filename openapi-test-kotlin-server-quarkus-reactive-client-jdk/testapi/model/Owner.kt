package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotNull

data class Owner(
  @field:JsonProperty("address") val address: Address?,
  @field:JsonProperty("email") @jakarta.validation.constraints.Email val email: String?,
  @field:JsonProperty("id") @NotNull val id: String,
  @field:JsonProperty("name") @NotNull val name: String
)