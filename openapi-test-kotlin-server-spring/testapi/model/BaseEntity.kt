package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime
import java.util.Optional

data class BaseEntity(
  @JsonProperty("createdAt") @NotNull val createdAt: OffsetDateTime,
  @JsonProperty("id") @NotNull val id: String,
  @JsonProperty("updatedAt") val updatedAt: Optional<OffsetDateTime>
)