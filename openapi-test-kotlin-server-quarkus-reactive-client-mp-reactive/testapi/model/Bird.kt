package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime
import java.util.Optional

data class Bird(
  @JsonProperty("id") @NotNull val id: String,
  @JsonProperty("createdAt") @NotNull val createdAt: OffsetDateTime,
  @JsonProperty("name") @NotNull val name: String,
  @JsonProperty("updatedAt") val updatedAt: Optional<OffsetDateTime>,
  @JsonProperty("wingSpan") val wingSpan: Optional<Double>,
  @JsonProperty("canFly") @NotNull val canFly: Boolean
) : Animal {
  override fun animal_type(): String = "bird"
}