package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.Optional

data class OptionalInfo(
  @JsonProperty("optional_field") val optional_field: Optional<String>,
  @JsonProperty("optional_with_default") val optional_with_default: Optional<String>,
  @JsonProperty("required_field") val required_field: Optional<String>
)