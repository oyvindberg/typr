package testapi.api

import com.fasterxml.jackson.databind.JsonNode
import io.smallrye.mutiny.Uni
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.lang.IllegalStateException
import java.lang.Void
import java.util.Optional
import kotlin.collections.List
import testapi.api.Response200404.Status200
import testapi.api.Response201400.Status201
import testapi.api.Response201400.Status400
import testapi.api.Response404Default.Status404
import testapi.api.Response404Default.StatusDefault
import testapi.model.Error
import testapi.model.Pet
import testapi.model.PetCreate

interface PetsApiServer : PetsApi {
  /** Create a pet */
  override fun createPet(body: PetCreate): Uni<Response201400<Pet, Error>>

  /** Endpoint wrapper for createPet - handles response status codes */
  @POST
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @SecurityRequirement(name = "oauth2", scopes = ["write:pets"])
  @SecurityRequirement(name = "apiKeyHeader")
  fun createPetEndpoint(body: PetCreate): Uni<Response> = createPet(body).map({ response: Response201400 -> when (val __r = response) {
    is Status201 -> { val r = __r as Status201; Response.ok(r.value).build() }
    is Status400 -> { val r = __r as Status400; Response.status(400).entity(r.value).build() }
    else -> throw IllegalStateException("Unexpected response type")
  } })

  /** Delete a pet */
  override fun deletePet(
    /** The pet ID */
    petId: String
  ): Uni<Response404Default<Error>>

  /** Endpoint wrapper for deletePet - handles response status codes */
  @DELETE
  @Path("/{petId}")
  fun deletePetEndpoint(
    /** The pet ID */
    petId: String
  ): Uni<Response> = deletePet(petId).map({ response: Response404Default -> when (val __r = response) {
    is Status404 -> { val r = __r as Status404; Response.status(404).entity(r.value).build() }
    is StatusDefault -> { val r = __r as StatusDefault; Response.status(r.statusCode).entity(r.value).build() }
    else -> throw IllegalStateException("Unexpected response type")
  } })

  /** Get a pet by ID */
  override fun getPet(
    /** The pet ID */
    petId: String
  ): Uni<Response200404<Pet, Error>>

  /** Endpoint wrapper for getPet - handles response status codes */
  @GET
  @Path("/{petId}")
  @Produces(MediaType.APPLICATION_JSON)
  fun getPetEndpoint(
    /** The pet ID */
    petId: String
  ): Uni<Response> = getPet(petId).map({ response: Response200404 -> when (val __r = response) {
    is Status200 -> { val r = __r as Status200; Response.ok(r.value).build() }
    is testapi.api.Response200404.Status404 -> { val r = __r as testapi.api.Response200404.Status404; Response.status(404).entity(r.value).build() }
    else -> throw IllegalStateException("Unexpected response type")
  } })

  /** Get pet photo */
  @GET
  @Path("/{petId}/photo")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  override fun getPetPhoto(
    /** The pet ID */
    petId: String
  ): Uni<Void>

  /** List all pets */
  @GET
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  override fun listPets(
    /** Maximum number of pets to return */
    limit: Optional<Integer>,
    /** Filter by status */
    status: Optional<String>
  ): Uni<List<Pet>>

  /** Upload a pet photo */
  @POST
  @Path("/{petId}/photo")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  override fun uploadPetPhoto(
    /** The pet ID */
    petId: String,
    /** Optional caption for the photo */
    caption: String,
    /** The photo file to upload */
    file: Array<Byte>
  ): Uni<JsonNode>
}