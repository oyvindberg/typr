package testapi.api

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import testapi.model.Animal

@RegisterRestClient
@Path("/animals")
trait AnimalsApiClient extends AnimalsApi {

  /** List all animals (polymorphic) */
  @GET
  @Path("/")
  @Produces(value = Array(MediaType.APPLICATION_JSON))
  def listAnimalsRaw: Response

  /** List all animals (polymorphic) - handles response status codes */
  override def listAnimals: Response2004XX5XX[List[Animal]] = {
    var response: jakarta.ws.rs.core.Response = null
    try {
      response = listAnimalsRaw;
    } catch {
      case e: jakarta.ws.rs.WebApplicationException => response = e.getResponse();
    }
    if (response.getStatus() == 200) new testapi.api.Ok(response.readEntity(new jakarta.ws.rs.core.GenericType[scala.List[testapi.model.Animal]] {}))
    else if (response.getStatus() >= 400 && response.getStatus() < 500) new testapi.api.ClientError4XX(response.getStatus(), response.readEntity(classOf[testapi.model.Error]))
    else if (response.getStatus() >= 500 && response.getStatus() < 600) new testapi.api.ServerError5XX(response.getStatus(), response.readEntity(classOf[testapi.model.Error]))
    else throw new java.lang.IllegalStateException("Unexpected status code: " + response.getStatus())
  }
}
