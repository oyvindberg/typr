package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

case class Cat(
  @JsonProperty("meowVolume") meowVolume: Option[Int],
  @JsonProperty("id") id: PetId,
  @JsonProperty("createdAt") createdAt: OffsetDateTime,
  /** Whether the cat is an indoor cat */
  @JsonProperty("indoor") indoor: Boolean,
  @JsonProperty("name") name: String,
  @JsonProperty("updatedAt") updatedAt: Option[OffsetDateTime]
) extends Animal {
  @JsonProperty("animal_type")
  override lazy val animal_type: String = "cat"
}