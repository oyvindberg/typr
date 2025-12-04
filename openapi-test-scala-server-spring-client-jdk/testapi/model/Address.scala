package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty

case class Address(
  @JsonProperty("city") city: Option[String],
  @JsonProperty("country") country: Option[String],
  @JsonProperty("street") street: Option[String],
  @JsonProperty("zipCode") zipCode: Option[String]
)