package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty

case class Owner(
  @JsonProperty("address") address: Option[Address],
  @JsonProperty("email") email: Option[String],
  @JsonProperty("id") id: String,
  @JsonProperty("name") name: String
)