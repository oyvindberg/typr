package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.circe.Json
import java.util.Optional

case class Error(
  @JsonProperty("code") code: String,
  @JsonProperty("details") details: Optional[java.util.Map[String, Json]],
  @JsonProperty("message") message: String
)