package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import java.util.Optional

case class Dog(
  @JsonProperty("id") id: PetId,
  @JsonProperty("name") name: String,
  @JsonProperty("updatedAt") updatedAt: Optional[OffsetDateTime],
  @JsonProperty("breed") breed: String,
  @JsonProperty("createdAt") createdAt: OffsetDateTime,
  @JsonProperty("barkVolume") barkVolume: Optional[Integer]
) extends Animal {
  @JsonProperty("animal_type")
  override lazy val animal_type: String = "dog"
}