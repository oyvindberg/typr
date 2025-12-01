package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotNull
import java.util.Optional

data class Owner(
  @JsonProperty("address") val address: Optional<Address>,
  @JsonProperty("email") @jakarta.validation.constraints.Email val email: Optional<String>,
  @JsonProperty("id") @NotNull val id: String,
  @JsonProperty("name") @NotNull val name: String
)