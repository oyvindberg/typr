package testapi.api

import com.fasterxml.jackson.databind.ObjectMapper
import java.lang.Exception
import java.net.URI
import java.net.http.HttpClient
import testapi.model.Animal

/** JDK HTTP Client implementation for AnimalsApi */
class AnimalsApiClient(
    /** JDK HTTP client for making HTTP requests */
    val httpClient: HttpClient,
    /** Base URI for API requests */
    val baseUri: URI,
    /** Jackson ObjectMapper for JSON serialization */
    val objectMapper: ObjectMapper
) extends AnimalsApi {

  /** List all animals (polymorphic) */
  @throws[Exception]
  override def listAnimals: Response2004XX5XX[java.util.List[Animal]] = {
    var request = java.net.http.HttpRequest
      .newBuilder(java.net.URI.create(baseUri.toString() + "/" + "animals"))
      .method("GET", java.net.http.HttpRequest.BodyPublishers.noBody())
      .header("Content-Type", "application/json")
      .header("Accept", "application/json")
      .build()
    var response = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString())
    var statusCode = response.statusCode()
    if (statusCode == 200) return new testapi.api.Ok(objectMapper.readValue(response.body(), new com.fasterxml.jackson.core.`type`.TypeReference[java.util.List[testapi.model.Animal]] {}))
    else if (statusCode >= 400 && statusCode < 500) return new testapi.api.ClientError4XX(statusCode, objectMapper.readValue(response.body(), classOf[testapi.model.Error]))
    else if (statusCode >= 500 && statusCode < 600) return new testapi.api.ServerError5XX(statusCode, objectMapper.readValue(response.body(), classOf[testapi.model.Error]))
    else throw new java.lang.IllegalStateException(s"Unexpected status code: ${statusCode}")
  }
}
