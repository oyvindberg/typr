package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotNull
import Int
import java.time.OffsetDateTime

data class Dog(
  @field:JsonProperty("id") @NotNull val id: PetId,
  @field:JsonProperty("name") @NotNull val name: String,
  @field:JsonProperty("updatedAt") val updatedAt: OffsetDateTime?,
  @field:JsonProperty("breed") @NotNull val breed: String,
  @field:JsonProperty("createdAt") @NotNull val createdAt: OffsetDateTime,
  @field:JsonProperty("barkVolume") val barkVolume: Integer?
) : Animal {
  override fun animal_type(): String = "dog"
}