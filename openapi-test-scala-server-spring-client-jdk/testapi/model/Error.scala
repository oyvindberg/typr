package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.circe.Json

case class Error(
  @JsonProperty("code") code: String,
  @JsonProperty("details") details: Option[Map[String, Json]],
  @JsonProperty("message") message: String
)