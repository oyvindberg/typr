package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import java.util.Optional

case class Cat(
    @JsonProperty("meowVolume") meowVolume: Optional[Integer],
    @JsonProperty("id") id: PetId,
    @JsonProperty("createdAt") createdAt: OffsetDateTime,
    /** Whether the cat is an indoor cat */
    @JsonProperty("indoor") indoor: java.lang.Boolean,
    @JsonProperty("name") name: String,
    @JsonProperty("updatedAt") updatedAt: Optional[OffsetDateTime]
) extends Animal {
  @JsonProperty("animal_type")
  override lazy val animal_type: String = "cat"
}
