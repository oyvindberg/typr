package testapi.api

import com.fasterxml.jackson.databind.JsonNode
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
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
import testapi.api.CreatePetResponse.Status201
import testapi.api.CreatePetResponse.Status400
import testapi.api.DeletePetResponse.Status404
import testapi.api.DeletePetResponse.StatusDefault
import testapi.api.GetPetResponse.Status200
import testapi.model.Pet
import testapi.model.PetCreate

@Path("/pets")
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
@SecurityScheme(name = "apiKeyHeader", type = SecuritySchemeType.APIKEY, `in` = SecuritySchemeIn.HEADER, paramName = "X-API-Key")
@SecurityScheme(name = "apiKeyQuery", type = SecuritySchemeType.APIKEY, `in` = SecuritySchemeIn.QUERY, paramName = "api_key")
@SecurityScheme(name = "oauth2", type = SecuritySchemeType.OAUTH2)
sealed interface PetsApiServer : PetsApi {
  /** Create a pet */
  override fun createPet(body: PetCreate): CreatePetResponse

  /** Endpoint wrapper for createPet - handles response status codes */
  @POST
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @SecurityRequirement(name = "oauth2", scopes = ["write:pets"])
  @SecurityRequirement(name = "apiKeyHeader")
  fun createPetEndpoint(body: PetCreate): Response = when (val __r = createPet(body)) {
    is Status201 -> { val r = __r as Status201; Response.ok(r.value).build() }
    is Status400 -> { val r = __r as Status400; Response.status(400).entity(r.value).build() }
    else -> throw IllegalStateException("Unexpected response type")
  }

  /** Delete a pet */
  override fun deletePet(
    /** The pet ID */
    petId: String
  ): DeletePetResponse

  /** Endpoint wrapper for deletePet - handles response status codes */
  @DELETE
  @Path("/{petId}")
  fun deletePetEndpoint(
    /** The pet ID */
    petId: String
  ): Response = when (val __r = deletePet(petId)) {
    is Status404 -> { val r = __r as Status404; Response.status(404).entity(r.value).build() }
    is StatusDefault -> { val r = __r as StatusDefault; Response.status(r.statusCode).entity(r.value).build() }
    else -> throw IllegalStateException("Unexpected response type")
  }

  /** Get a pet by ID */
  override fun getPet(
    /** The pet ID */
    petId: String
  ): GetPetResponse

  /** Endpoint wrapper for getPet - handles response status codes */
  @GET
  @Path("/{petId}")
  @Produces(MediaType.APPLICATION_JSON)
  fun getPetEndpoint(
    /** The pet ID */
    petId: String
  ): Response = when (val __r = getPet(petId)) {
    is Status200 -> { val r = __r as Status200; Response.ok(r.value).build() }
    is testapi.api.GetPetResponse.Status404 -> { val r = __r as testapi.api.GetPetResponse.Status404; Response.status(404).entity(r.value).build() }
    else -> throw IllegalStateException("Unexpected response type")
  }

  /** Get pet photo */
  @GET
  @Path("/{petId}/photo")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  override fun getPetPhoto(
    /** The pet ID */
    petId: String
  ): Void

  /** List all pets */
  @GET
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  override fun listPets(
    /** Maximum number of pets to return */
    limit: Optional<Integer>,
    /** Filter by status */
    status: Optional<String>
  ): List<Pet>

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
  ): JsonNode
}