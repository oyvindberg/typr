package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotNull

data class Money(
  @JsonProperty("amount") @NotNull val amount: Double,
  @JsonProperty("currency") @NotNull val currency: Currency
)