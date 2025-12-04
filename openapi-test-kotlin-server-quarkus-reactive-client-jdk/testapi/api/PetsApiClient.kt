package testapi.api

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.smallrye.mutiny.Uni
import java.lang.Exception
import java.lang.IllegalStateException
import java.lang.RuntimeException
import java.lang.Void
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers
import java.util.Optional
import kotlin.collections.List
import testapi.model.Error
import testapi.model.Pet
import testapi.model.PetCreate
import testapi.model.PetId
import typo.runtime.internal.stringInterpolator.str

/** JDK HTTP Client implementation for PetsApi */
data class PetsApiClient(
  /** JDK HTTP client for making HTTP requests */
  val httpClient: HttpClient,
  /** Base URI for API requests */
  val baseUri: URI,
  /** Jackson ObjectMapper for JSON serialization */
  val objectMapper: ObjectMapper
) : PetsApi {
  /** Create a pet */
  override fun createPet(body: PetCreate): Uni<Response201400<Pet, Error>> {
    try {
      var request = HttpRequest.newBuilder(URI.create(baseUri.toString() + "/" + "pets")).method("POST", BodyPublishers.ofString(objectMapper.writeValueAsString(body))).header("Content-Type", "application/json").header("Accept", "application/json").build()
      return Uni.createFrom().completionStage(() -> httpClient.sendAsync(request, BodyHandlers.ofString())).map({ response -> try {
        var statusCode = response.statusCode()
        if (statusCode == 201) { return Created(objectMapper.readValue(response.body(), Pet::class.java)) }
        else if (statusCode == 400) { return BadRequest(objectMapper.readValue(response.body(), Error::class.java)) }
        else { throw IllegalStateException(str("Unexpected status code: ", statusCode.toString(), "")) }
      } catch (e: Exception) {
        throw RuntimeException(e)
      }  })
    } catch (e: Exception) {
      throw RuntimeException(e)
    } 
  }

  /** Delete a pet */
  override fun deletePet(
    /** The pet ID */
    petId: PetId
  ): Uni<Void> {
    var request = HttpRequest.newBuilder(URI.create(baseUri.toString() + "/" + "pets" + "/" + petId.toString())).method("DELETE", BodyPublishers.noBody()).header("Content-Type", "application/json").header("Accept", "application/json").build()
    return Uni.createFrom().completionStage(() -> httpClient.sendAsync(request, BodyHandlers.ofString())).map({ response -> null })
  }

  /** Get a pet by ID */
  override fun getPet(
    /** The pet ID */
    petId: PetId
  ): Uni<Response200404<Pet, Error>> {
    var request = HttpRequest.newBuilder(URI.create(baseUri.toString() + "/" + "pets" + "/" + petId.toString())).method("GET", BodyPublishers.noBody()).header("Content-Type", "application/json").header("Accept", "application/json").build()
    return Uni.createFrom().completionStage(() -> httpClient.sendAsync(request, BodyHandlers.ofString())).map({ response -> try {
      var statusCode = response.statusCode()
      if (statusCode == 200) { return Ok(objectMapper.readValue(response.body(), Pet::class.java)) }
      else if (statusCode == 404) { return NotFound(objectMapper.readValue(response.body(), Error::class.java)) }
      else { throw IllegalStateException(str("Unexpected status code: ", statusCode.toString(), "")) }
    } catch (e: Exception) {
      throw RuntimeException(e)
    }  })
  }

  /** Get pet photo */
  override fun getPetPhoto(
    /** The pet ID */
    petId: PetId
  ): Uni<Void> {
    var request = HttpRequest.newBuilder(URI.create(baseUri.toString() + "/" + "pets" + "/" + petId.toString() + "/" + "photo")).method("GET", BodyPublishers.noBody()).header("Content-Type", "application/json").header("Accept", "application/json").build()
    return Uni.createFrom().completionStage(() -> httpClient.sendAsync(request, BodyHandlers.ofString())).map({ response -> null })
  }

  /** List all pets */
  override fun listPets(
    /** Maximum number of pets to return */
    limit: Optional<Integer>,
    /** Filter by status */
    status: Optional<String>
  ): Uni<List<Pet>> {
    var request = HttpRequest.newBuilder(URI.create(baseUri.toString() + "/" + "pets" + (if (limit.isPresent()) "?limit=" + limit.get().toString() else "") + (if (status.isPresent()) "&status=" + status.get().toString() else ""))).method("GET", BodyPublishers.noBody()).header("Content-Type", "application/json").header("Accept", "application/json").build()
    return Uni.createFrom().completionStage(() -> httpClient.sendAsync(request, BodyHandlers.ofString())).map({ response -> try {
      return objectMapper.readValue(response.body(), object : TypeReference<List<Pet>>() {})
    } catch (e: Exception) {
      throw RuntimeException(e)
    }  })
  }

  /** Upload a pet photo */
  override fun uploadPetPhoto(
    /** The pet ID */
    petId: PetId,
    /** Optional caption for the photo */
    caption: String,
    /** The photo file to upload */
    file: Array<Byte>
  ): Uni<JsonNode> {
    var request = HttpRequest.newBuilder(URI.create(baseUri.toString() + "/" + "pets" + "/" + petId.toString() + "/" + "photo")).method("POST", BodyPublishers.noBody()).header("Content-Type", "application/json").header("Accept", "application/json").build()
    return Uni.createFrom().completionStage(() -> httpClient.sendAsync(request, BodyHandlers.ofString())).map({ response -> try {
      return objectMapper.readValue(response.body(), JsonNode::class.java)
    } catch (e: Exception) {
      throw RuntimeException(e)
    }  })
  }
}