package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

case class Dog(
  @JsonProperty("id") id: PetId,
  @JsonProperty("name") name: String,
  @JsonProperty("updatedAt") updatedAt: Option[OffsetDateTime],
  @JsonProperty("breed") breed: String,
  @JsonProperty("createdAt") createdAt: OffsetDateTime,
  @JsonProperty("barkVolume") barkVolume: Option[Int]
) extends Animal {
  @JsonProperty("animal_type")
  override lazy val animal_type: String = "dog"
}