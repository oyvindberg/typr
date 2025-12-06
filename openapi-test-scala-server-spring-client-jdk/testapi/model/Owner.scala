package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.Optional

case class Owner(
    @JsonProperty("address") address: Optional[Address],
    @JsonProperty("email") email: Optional[String],
    @JsonProperty("id") id: String,
    @JsonProperty("name") name: String
)
