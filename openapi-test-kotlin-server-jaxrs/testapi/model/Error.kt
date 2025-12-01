package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import jakarta.validation.constraints.NotNull
import java.util.Optional
import kotlin.collections.Map

data class Error(
  @JsonProperty("code") @NotNull val code: String,
  @JsonProperty("details") val details: Optional<Map<String, JsonNode>>,
  @JsonProperty("message") @NotNull val message: String
)