package testapi.api

import io.smallrye.mutiny.Uni
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.lang.IllegalStateException
import kotlin.collections.List
import testapi.api.Response2004XX5XX.Status200
import testapi.api.Response2004XX5XX.Status4XX
import testapi.api.Response2004XX5XX.Status5XX
import testapi.model.Animal

interface AnimalsApiServer : AnimalsApi {
  /** List all animals (polymorphic) */
  override fun listAnimals(): Uni<Response2004XX5XX<List<Animal>>>

  /** Endpoint wrapper for listAnimals - handles response status codes */
  @GET
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  fun listAnimalsEndpoint(): Uni<Response> = listAnimals().map({ response: Response2004XX5XX -> when (val __r = response) {
    is Status200 -> { val r = __r as Status200; Response.ok(r.value).build() }
    is Status4XX -> { val r = __r as Status4XX; Response.status(r.statusCode).entity(r.value).build() }
    is Status5XX -> { val r = __r as Status5XX; Response.status(r.statusCode).entity(r.value).build() }
    else -> throw IllegalStateException("Unexpected response type")
  } })
}