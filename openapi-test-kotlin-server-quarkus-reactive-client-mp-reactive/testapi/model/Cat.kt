package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime
import java.util.Optional

data class Cat(
  @JsonProperty("meowVolume") val meowVolume: Optional<Integer>,
  @JsonProperty("id") @NotNull val id: String,
  @JsonProperty("createdAt") @NotNull val createdAt: OffsetDateTime,
  /** Whether the cat is an indoor cat */
  @JsonProperty("indoor") @NotNull val indoor: Boolean,
  @JsonProperty("name") @NotNull val name: String,
  @JsonProperty("updatedAt") val updatedAt: Optional<OffsetDateTime>
) : Animal {
  override fun animal_type(): String = "cat"
}