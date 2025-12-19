package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import java.util.Optional

case class Bird(
  @JsonProperty("id") id: PetId,
  @JsonProperty("createdAt") createdAt: OffsetDateTime,
  @JsonProperty("name") name: String,
  @JsonProperty("updatedAt") updatedAt: Optional[OffsetDateTime],
  @JsonProperty("wingSpan") wingSpan: Optional[java.lang.Double],
  @JsonProperty("canFly") canFly: java.lang.Boolean
) extends Animal {
  @JsonProperty("animal_type")
  override lazy val animal_type: String = "bird"
}