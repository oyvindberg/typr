package testapi.api;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.lang.Void;
import java.util.List;
import java.util.Optional;
import testapi.model.Pet;
import testapi.model.PetCreate;

@Path("/pets")
public sealed interface PetsApi {
  @POST
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  /** Create a pet */
  CreatePetResponse createPet(PetCreate body);

  @DELETE
  @Path("/{petId}")
  /** Delete a pet */
  Void deletePet(
  
    /** The pet ID */
    @PathParam("petId") String petId
  );

  @GET
  @Path("/{petId}")
  @Produces(MediaType.APPLICATION_JSON)
  /** Get a pet by ID */
  GetPetResponse getPet(
  
    /** The pet ID */
    @PathParam("petId") String petId
  );

  @GET
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  /** List all pets */
  List<Pet> listPets(
    /** Maximum number of pets to return */
    @QueryParam("limit") @DefaultValue("20") Optional<Integer> limit,
    /** Filter by status */
    @QueryParam("status") @DefaultValue("available") Optional<String> status
  );
}