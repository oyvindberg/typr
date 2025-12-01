package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime
import java.util.Optional

data class Dog(
  @JsonProperty("id") @NotNull val id: String,
  @JsonProperty("name") @NotNull val name: String,
  @JsonProperty("updatedAt") val updatedAt: Optional<OffsetDateTime>,
  @JsonProperty("breed") @NotNull val breed: String,
  @JsonProperty("createdAt") @NotNull val createdAt: OffsetDateTime,
  @JsonProperty("barkVolume") val barkVolume: Optional<Integer>
) : Animal {
  override fun animal_type(): String = "dog"
}