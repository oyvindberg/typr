package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty

data class OptionalInfo(
  @field:JsonProperty("optional_field") val optional_field: String?,
  @field:JsonProperty("optional_with_default") val optional_with_default: String?,
  @field:JsonProperty("required_field") val required_field: String?
)