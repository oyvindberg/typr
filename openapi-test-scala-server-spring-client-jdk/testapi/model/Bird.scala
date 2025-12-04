package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

case class Bird(
  @JsonProperty("id") id: PetId,
  @JsonProperty("createdAt") createdAt: OffsetDateTime,
  @JsonProperty("name") name: String,
  @JsonProperty("updatedAt") updatedAt: Option[OffsetDateTime],
  @JsonProperty("wingSpan") wingSpan: Option[Double],
  @JsonProperty("canFly") canFly: Boolean
) extends Animal {
  @JsonProperty("animal_type")
  override lazy val animal_type: String = "bird"
}