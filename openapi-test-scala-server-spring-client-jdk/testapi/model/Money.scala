package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty

case class Money(
  @JsonProperty("amount") amount: Double,
  @JsonProperty("currency") currency: Currency
)