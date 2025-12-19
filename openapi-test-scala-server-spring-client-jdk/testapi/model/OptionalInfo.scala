package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.Optional

case class OptionalInfo(
  @JsonProperty("optional_field") optional_field: Optional[String],
  @JsonProperty("optional_with_default") optional_with_default: Optional[String],
  @JsonProperty("required_field") required_field: Optional[String]
)