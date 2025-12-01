package testapi.api

import io.smallrye.mutiny.Uni
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.GenericType
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.lang.IllegalStateException
import kotlin.collections.List
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import testapi.api.ListAnimalsResponse.Status200
import testapi.api.ListAnimalsResponse.Status4XX
import testapi.api.ListAnimalsResponse.Status5XX
import testapi.model.Animal
import testapi.model.Error

@RegisterRestClient
@Path("/animals")
sealed interface AnimalsApiClient : AnimalsApi {
  /** List all animals (polymorphic) - handles response status codes */
  override fun listAnimals(): Uni<ListAnimalsResponse> = listAnimalsRaw().map({ response: Response -> if (response.getStatus() == 200) { Status200(response.readEntity(object : GenericType<List<Animal>>() {})) }
  else if (response.getStatus() >= 400 && response.getStatus() < 500) { Status4XX(response.getStatus(), response.readEntity(Error::class.java)) }
  else if (response.getStatus() >= 500 && response.getStatus() < 600) { Status5XX(response.getStatus(), response.readEntity(Error::class.java)) }
  else { throw IllegalStateException("Unexpected status code: " + response.getStatus()) } }).onFailure(WebApplicationException::class.java).recoverWithItem({ e: WebApplicationException -> if (e.getResponse().getStatus() == 200) { Status200(e.getResponse().readEntity(object : GenericType<List<Animal>>() {})) }
  else if (e.getResponse().getStatus() >= 400 && e.getResponse().getStatus() < 500) { Status4XX(e.getResponse().getStatus(), e.getResponse().readEntity(Error::class.java)) }
  else if (e.getResponse().getStatus() >= 500 && e.getResponse().getStatus() < 600) { Status5XX(e.getResponse().getStatus(), e.getResponse().readEntity(Error::class.java)) }
  else { throw IllegalStateException("Unexpected status code: " + e.getResponse().getStatus()) } })

  /** List all animals (polymorphic) */
  @GET
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  fun listAnimalsRaw(): Uni<Response>
}