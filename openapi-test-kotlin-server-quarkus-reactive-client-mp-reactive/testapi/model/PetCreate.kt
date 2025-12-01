package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.util.Optional
import kotlin.collections.List

data class PetCreate(
  @JsonProperty("age") @Min(0L) @Max(100L) val age: Optional<Long>,
  @JsonProperty("email") @jakarta.validation.constraints.Email val email: Optional<String>,
  @JsonProperty("name") @NotNull @Size(min = 1, max = 100) val name: String,
  @JsonProperty("status") val status: Optional<PetStatus>,
  @JsonProperty("tags") @Size(min = 0, max = 10) val tags: Optional<List<String>>,
  @JsonProperty("website") @Pattern(regexp = "^https?://.*") val website: Optional<String>
)