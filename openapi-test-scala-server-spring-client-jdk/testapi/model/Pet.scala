package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import java.util.Optional

case class Pet(
    @JsonProperty("tags") tags: Optional[java.util.List[String]],
    @JsonProperty("id") id: PetId,
    @JsonProperty("status") status: PetStatus,
    @JsonProperty("createdAt") createdAt: OffsetDateTime,
    @JsonProperty("metadata") metadata: Optional[java.util.Map[String, String]],
    /** Pet name */
    @JsonProperty("name") name: String,
    @JsonProperty("updatedAt") updatedAt: Optional[OffsetDateTime]
)
