package testapi.api

import com.fasterxml.jackson.databind.JsonNode
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.lang.IllegalStateException
import java.lang.Void
import java.util.Optional
import kotlin.collections.List
import testapi.model.Error
import testapi.model.Pet
import testapi.model.PetCreate
import testapi.model.PetId

interface PetsApiClient : PetsApi {
  /** Create a pet - handles response status codes */
  override fun createPet(body: PetCreate): Response201400<Pet, Error> {
    var response: Response
    try {
      response = createPetRaw(body);
    } catch (e: WebApplicationException) {
      response = e.getResponse();
    } 
    if (response.getStatus() == 201) { return Created(response.readEntity(Pet::class.java)) }
    else if (response.getStatus() == 400) { return BadRequest(response.readEntity(Error::class.java)) }
    else { throw IllegalStateException("Unexpected status code: " + response.getStatus()) }
  }

  /** Create a pet */
  @POST
  @Path("/")
  @Consumes(value = [MediaType.APPLICATION_JSON])
  @Produces(value = [MediaType.APPLICATION_JSON])
  @SecurityRequirement(name = "oauth2", scopes = ["write:pets"])
  @SecurityRequirement(name = "apiKeyHeader")
  fun createPetRaw(body: PetCreate): Response

  /** Delete a pet */
  @DELETE
  @Path("/{petId}")
  override fun deletePet(
    /** The pet ID */
    petId: PetId
  ): Void

  /** Get a pet by ID - handles response status codes */
  override fun getPet(
    /** The pet ID */
    petId: PetId
  ): Response200404<Pet, Error> {
    var response: Response
    try {
      response = getPetRaw(petId);
    } catch (e: WebApplicationException) {
      response = e.getResponse();
    } 
    if (response.getStatus() == 200) { return Ok(response.readEntity(Pet::class.java)) }
    else if (response.getStatus() == 404) { return NotFound(response.readEntity(Error::class.java)) }
    else { throw IllegalStateException("Unexpected status code: " + response.getStatus()) }
  }

  /** Get pet photo */
  @GET
  @Path("/{petId}/photo")
  @Produces(value = [MediaType.APPLICATION_OCTET_STREAM])
  override fun getPetPhoto(
    /** The pet ID */
    petId: PetId
  ): Void

  /** Get a pet by ID */
  @GET
  @Path("/{petId}")
  @Produces(value = [MediaType.APPLICATION_JSON])
  fun getPetRaw(
    /** The pet ID */
    petId: PetId
  ): Response

  /** List all pets */
  @GET
  @Path("/")
  @Produces(value = [MediaType.APPLICATION_JSON])
  override fun listPets(
    /** Maximum number of pets to return */
    limit: Optional<Integer>,
    /** Filter by status */
    status: Optional<String>
  ): List<Pet>

  /** Upload a pet photo */
  @POST
  @Path("/{petId}/photo")
  @Consumes(value = [MediaType.MULTIPART_FORM_DATA])
  @Produces(value = [MediaType.APPLICATION_JSON])
  override fun uploadPetPhoto(
    /** The pet ID */
    petId: PetId,
    /** Optional caption for the photo */
    caption: String,
    /** The photo file to upload */
    file: Array<Byte>
  ): JsonNode
}