package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import jakarta.validation.constraints.NotNull
import kotlin.collections.Map

data class Error(
  @field:JsonProperty("code") @NotNull val code: String,
  @field:JsonProperty("details") val details: Map<String, JsonNode>?,
  @field:JsonProperty("message") @NotNull val message: String
)