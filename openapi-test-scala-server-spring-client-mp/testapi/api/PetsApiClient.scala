package testapi.api

import io.circe.Json
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.DefaultValue
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.lang.Void
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import org.glassfish.jersey.media.multipart.FormDataParam
import testapi.model.Error
import testapi.model.Pet
import testapi.model.PetCreate
import testapi.model.PetId

@RegisterRestClient
@Path("/pets")
trait PetsApiClient extends PetsApi {

  /** Create a pet */
  @POST
  @Path("/")
  @Consumes(value = Array(MediaType.APPLICATION_JSON))
  @Produces(value = Array(MediaType.APPLICATION_JSON))
  @SecurityRequirement(name = "oauth2", scopes = Array("write:pets"))
  @SecurityRequirement(name = "apiKeyHeader")
  def createPetRaw(body: PetCreate): Response

  /** Create a pet - handles response status codes */
  override def createPet(body: PetCreate): Response201400[Pet, Error] = {
    var response: jakarta.ws.rs.core.Response = null
    try {
      response = createPetRaw(body);
    } catch {
      case e: jakarta.ws.rs.WebApplicationException => response = e.getResponse();
    }
    if (response.getStatus() == 201) new testapi.api.Created(response.readEntity(classOf[testapi.model.Pet]))
    else if (response.getStatus() == 400) new testapi.api.BadRequest(response.readEntity(classOf[testapi.model.Error]))
    else throw new java.lang.IllegalStateException("Unexpected status code: " + response.getStatus())
  }

  /** Delete a pet */
  @DELETE
  @Path("/{petId}")
  override def deletePet(
      /** The pet ID */
      @PathParam("petId") petId: PetId
  ): Void

  /** Get a pet by ID */
  @GET
  @Path("/{petId}")
  @Produces(value = Array(MediaType.APPLICATION_JSON))
  def getPetRaw(
      /** The pet ID */
      @PathParam("petId") petId: PetId
  ): Response

  /** Get a pet by ID - handles response status codes */
  override def getPet(
      /** The pet ID */
      petId: PetId
  ): Response200404[Pet, Error] = {
    var response: jakarta.ws.rs.core.Response = null
    try {
      response = getPetRaw(petId);
    } catch {
      case e: jakarta.ws.rs.WebApplicationException => response = e.getResponse();
    }
    if (response.getStatus() == 200) new testapi.api.Ok(response.readEntity(classOf[testapi.model.Pet]))
    else if (response.getStatus() == 404) new testapi.api.NotFound(response.readEntity(classOf[testapi.model.Error]))
    else throw new java.lang.IllegalStateException("Unexpected status code: " + response.getStatus())
  }

  /** Get pet photo */
  @GET
  @Path("/{petId}/photo")
  @Produces(value = Array(MediaType.APPLICATION_OCTET_STREAM))
  override def getPetPhoto(
      /** The pet ID */
      @PathParam("petId") petId: PetId
  ): Void

  /** List all pets */
  @GET
  @Path("/")
  @Produces(value = Array(MediaType.APPLICATION_JSON))
  override def listPets(
      /** Maximum number of pets to return */
      @QueryParam("limit") @DefaultValue("20") limit: Option[Int],
      /** Filter by status */
      @QueryParam("status") @DefaultValue("available") status: Option[String]
  ): List[Pet]

  /** Upload a pet photo */
  @POST
  @Path("/{petId}/photo")
  @Consumes(value = Array(MediaType.MULTIPART_FORM_DATA))
  @Produces(value = Array(MediaType.APPLICATION_JSON))
  override def uploadPetPhoto(
      /** The pet ID */
      @PathParam("petId") petId: PetId,
      /** Optional caption for the photo */
      @FormDataParam("caption") caption: String,
      /** The photo file to upload */
      @FormDataParam("file") file: Array[Byte]
  ): Json
}
