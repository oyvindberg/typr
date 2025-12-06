package testapi.api

import com.fasterxml.jackson.databind.ObjectMapper
import io.circe.Json
import java.lang.Exception
import java.net.URI
import java.net.http.HttpClient
import java.util.Optional
import testapi.model.Error
import testapi.model.Pet
import testapi.model.PetCreate
import testapi.model.PetId

/** JDK HTTP Client implementation for PetsApi */
class PetsApiClient(
  /** JDK HTTP client for making HTTP requests */
  val httpClient: HttpClient,
  /** Base URI for API requests */
  val baseUri: URI,
  /** Jackson ObjectMapper for JSON serialization */
  val objectMapper: ObjectMapper
) extends PetsApi {
  /** Create a pet */
  @throws[Exception]
  override def createPet(body: PetCreate): Response201400[Pet, Error] = {
    var request = java.net.http.HttpRequest.newBuilder(java.net.URI.create(baseUri.toString() + "/" + "pets")).method("POST", java.net.http.HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body))).header("Content-Type", "application/json").header("Accept", "application/json").build()
    var response = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString())
    var statusCode = response.statusCode()
    if (statusCode == 201) return new testapi.api.Created(objectMapper.readValue(response.body(), classOf[testapi.model.Pet]))
    else if (statusCode == 400) return new testapi.api.BadRequest(objectMapper.readValue(response.body(), classOf[testapi.model.Error]))
    else throw new java.lang.IllegalStateException(s"Unexpected status code: ${statusCode}")
  }

  /** Delete a pet */
  @throws[Exception]
  override def deletePet(
    /** The pet ID */
    petId: PetId
  ): Unit = {
    var request = java.net.http.HttpRequest.newBuilder(java.net.URI.create(baseUri.toString() + "/" + "pets" + "/" + petId.toString())).method("DELETE", java.net.http.HttpRequest.BodyPublishers.noBody()).header("Content-Type", "application/json").header("Accept", "application/json").build()
    var response = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString())
  }

  /** Get a pet by ID */
  @throws[Exception]
  override def getPet(
    /** The pet ID */
    petId: PetId
  ): Response200404[Pet, Error] = {
    var request = java.net.http.HttpRequest.newBuilder(java.net.URI.create(baseUri.toString() + "/" + "pets" + "/" + petId.toString())).method("GET", java.net.http.HttpRequest.BodyPublishers.noBody()).header("Content-Type", "application/json").header("Accept", "application/json").build()
    var response = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString())
    var statusCode = response.statusCode()
    if (statusCode == 200) return new testapi.api.Ok(objectMapper.readValue(response.body(), classOf[testapi.model.Pet]))
    else if (statusCode == 404) return new testapi.api.NotFound(objectMapper.readValue(response.body(), classOf[testapi.model.Error]))
    else throw new java.lang.IllegalStateException(s"Unexpected status code: ${statusCode}")
  }

  /** Get pet photo */
  @throws[Exception]
  override def getPetPhoto(
    /** The pet ID */
    petId: PetId
  ): Unit = {
    var request = java.net.http.HttpRequest.newBuilder(java.net.URI.create(baseUri.toString() + "/" + "pets" + "/" + petId.toString() + "/" + "photo")).method("GET", java.net.http.HttpRequest.BodyPublishers.noBody()).header("Content-Type", "application/json").header("Accept", "application/json").build()
    var response = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString())
  }

  /** List all pets */
  @throws[Exception]
  override def listPets(
    /** Maximum number of pets to return */
    limit: Optional[Integer],
    /** Filter by status */
    status: Optional[String]
  ): java.util.List[Pet] = {
    var request = java.net.http.HttpRequest.newBuilder(java.net.URI.create(baseUri.toString() + "/" + "pets" + (if (limit.isPresent()) "?limit=" + limit.get().toString() else "") + (if (status.isPresent()) "&status=" + status.get().toString() else ""))).method("GET", java.net.http.HttpRequest.BodyPublishers.noBody()).header("Content-Type", "application/json").header("Accept", "application/json").build()
    var response = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString())
    return objectMapper.readValue(response.body(), new com.fasterxml.jackson.core.`type`.TypeReference[java.util.List[testapi.model.Pet]] {})
  }

  /** Upload a pet photo */
  @throws[Exception]
  override def uploadPetPhoto(
    /** The pet ID */
    petId: PetId,
    /** Optional caption for the photo */
    caption: String,
    /** The photo file to upload */
    file: Array[Byte]
  ): Json = {
    var request = java.net.http.HttpRequest.newBuilder(java.net.URI.create(baseUri.toString() + "/" + "pets" + "/" + petId.toString() + "/" + "photo")).method("POST", java.net.http.HttpRequest.BodyPublishers.noBody()).header("Content-Type", "application/json").header("Accept", "application/json").build()
    var response = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString())
    return objectMapper.readValue(response.body(), classOf[io.circe.Json])
  }
}