package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import java.util.Optional

case class BaseEntity(
  @JsonProperty("createdAt") createdAt: OffsetDateTime,
  @JsonProperty("id") id: PetId,
  @JsonProperty("updatedAt") updatedAt: Optional[OffsetDateTime]
)