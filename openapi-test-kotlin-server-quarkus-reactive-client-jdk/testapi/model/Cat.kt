package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotNull
import Int
import java.time.OffsetDateTime

data class Cat(
  @field:JsonProperty("meowVolume") val meowVolume: Integer?,
  @field:JsonProperty("id") @NotNull val id: PetId,
  @field:JsonProperty("createdAt") @NotNull val createdAt: OffsetDateTime,
  /** Whether the cat is an indoor cat */
  @field:JsonProperty("indoor") @NotNull val indoor: Boolean,
  @field:JsonProperty("name") @NotNull val name: String,
  @field:JsonProperty("updatedAt") val updatedAt: OffsetDateTime?
) : Animal {
  override fun animal_type(): String = "cat"
}