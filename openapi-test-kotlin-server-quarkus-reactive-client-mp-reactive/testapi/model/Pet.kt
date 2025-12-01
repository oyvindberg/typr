package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime
import java.util.Optional
import kotlin.collections.List
import kotlin.collections.Map

data class Pet(
  @JsonProperty("tags") val tags: Optional<List<String>>,
  @JsonProperty("id") @NotNull val id: String,
  @JsonProperty("status") @NotNull val status: PetStatus,
  @JsonProperty("createdAt") @NotNull val createdAt: OffsetDateTime,
  @JsonProperty("metadata") val metadata: Optional<Map<String, String>>,
  /** Pet name */
  @JsonProperty("name") @NotNull val name: String,
  @JsonProperty("updatedAt") val updatedAt: Optional<OffsetDateTime>
)