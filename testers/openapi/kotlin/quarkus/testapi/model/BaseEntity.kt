package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime

data class BaseEntity(
  @field:JsonProperty("createdAt") @NotNull val createdAt: OffsetDateTime,
  @field:JsonProperty("id") @NotNull val id: PetId,
  @field:JsonProperty("updatedAt") val updatedAt: OffsetDateTime?
)