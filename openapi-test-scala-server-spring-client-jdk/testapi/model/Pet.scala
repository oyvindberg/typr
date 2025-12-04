package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

case class Pet(
  @JsonProperty("tags") tags: Option[List[String]],
  @JsonProperty("id") id: PetId,
  @JsonProperty("status") status: PetStatus,
  @JsonProperty("createdAt") createdAt: OffsetDateTime,
  @JsonProperty("metadata") metadata: Option[Map[String, String]],
  /** Pet name */
  @JsonProperty("name") name: String,
  @JsonProperty("updatedAt") updatedAt: Option[OffsetDateTime]
)