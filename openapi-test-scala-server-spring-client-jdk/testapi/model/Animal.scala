package testapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "animal_type")
@JsonSubTypes(value = Array(new Type(value = classOf[Cat], name = "cat"), new Type(value = classOf[Dog], name = "dog"), new Type(value = classOf[Bird], name = "bird")))
trait Animal {
  @JsonProperty("animal_type")
  def animal_type: String
}