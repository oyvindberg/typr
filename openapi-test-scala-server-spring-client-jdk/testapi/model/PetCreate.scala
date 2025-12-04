package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty

case class PetCreate(
  @JsonProperty("age") age: Option[Long],
  @JsonProperty("email") email: Option[String],
  @JsonProperty("name") name: String,
  @JsonProperty("status") status: Option[PetStatus],
  @JsonProperty("tags") tags: Option[List[String]],
  @JsonProperty("website") website: Option[String]
)