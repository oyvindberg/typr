package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

case class BaseEntity(
  @JsonProperty("createdAt") createdAt: OffsetDateTime,
  @JsonProperty("id") id: PetId,
  @JsonProperty("updatedAt") updatedAt: Option[OffsetDateTime]
)