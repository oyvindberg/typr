package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime

data class Bird(
  @field:JsonProperty("id") @NotNull val id: PetId,
  @field:JsonProperty("createdAt") @NotNull val createdAt: OffsetDateTime,
  @field:JsonProperty("name") @NotNull val name: String,
  @field:JsonProperty("updatedAt") val updatedAt: OffsetDateTime?,
  @field:JsonProperty("wingSpan") val wingSpan: Double?,
  @field:JsonProperty("canFly") @NotNull val canFly: Boolean
) : Animal {
  override fun animal_type(): String = "bird"
}