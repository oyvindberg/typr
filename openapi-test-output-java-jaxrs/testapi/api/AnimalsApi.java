package testapi.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import testapi.model.Animal;

@Path("/animals")
public sealed interface AnimalsApi {
  @GET
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  /** List all animals (polymorphic) */
  List<Animal> listAnimals();
}