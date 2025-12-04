package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty

case class OptionalInfo(
  @JsonProperty("optional_field") optional_field: Option[String],
  @JsonProperty("optional_with_default") optional_with_default: Option[String],
  @JsonProperty("required_field") required_field: Option[String]
)