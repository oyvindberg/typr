package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.Optional

case class Address(
  @JsonProperty("city") city: Optional[String],
  @JsonProperty("country") country: Optional[String],
  @JsonProperty("street") street: Optional[String],
  @JsonProperty("zipCode") zipCode: Optional[String]
)