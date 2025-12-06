package testapi.api

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.smallrye.mutiny.Uni
import java.lang.Exception
import java.lang.IllegalStateException
import java.lang.RuntimeException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers
import kotlin.collections.List
import testapi.model.Animal
import testapi.model.Error
import typo.runtime.internal.stringInterpolator.str

/** JDK HTTP Client implementation for AnimalsApi */
data class AnimalsApiClient(
  /** JDK HTTP client for making HTTP requests */
  val httpClient: HttpClient,
  /** Base URI for API requests */
  val baseUri: URI,
  /** Jackson ObjectMapper for JSON serialization */
  val objectMapper: ObjectMapper
) : AnimalsApi {
  /** List all animals (polymorphic) */
  override fun listAnimals(): Uni<Response2004XX5XX<List<Animal>>> {
    var request = HttpRequest.newBuilder(URI.create(baseUri.toString() + "/" + "animals")).method("GET", BodyPublishers.noBody()).header("Content-Type", "application/json").header("Accept", "application/json").build()
    return Uni.createFrom().completionStage({ httpClient.sendAsync(request, BodyHandlers.ofString()) }).map({ response -> try {
      var statusCode = response.statusCode()
      if (statusCode == 200) { Ok(objectMapper.readValue(response.body(), object : TypeReference<List<Animal>>() {})) }
      else if (statusCode >= 400 && statusCode < 500) { ClientError4XX(statusCode, objectMapper.readValue(response.body(), Error::class.java)) }
      else if (statusCode >= 500 && statusCode < 600) { ServerError5XX(statusCode, objectMapper.readValue(response.body(), Error::class.java)) }
      else { throw IllegalStateException(str("Unexpected status code: ", statusCode.toString(), "")) }
    } catch (e: Exception) {
      throw RuntimeException(e)
    }  })
  }
}