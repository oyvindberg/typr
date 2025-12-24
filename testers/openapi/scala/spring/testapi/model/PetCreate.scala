package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.Optional

case class PetCreate(
    @JsonProperty("age") age: Optional[java.lang.Long],
    @JsonProperty("email") email: Optional[String],
    @JsonProperty("name") name: String,
    @JsonProperty("status") status: Optional[PetStatus],
    @JsonProperty("tags") tags: Optional[java.util.List[String]],
    @JsonProperty("website") website: Optional[String]
)
