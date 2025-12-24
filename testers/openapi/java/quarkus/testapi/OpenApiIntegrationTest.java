package testapi;

import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import testapi.api.*;
import testapi.model.Pet;
import testapi.model.PetCreate;
import testapi.model.PetId;
import testapi.model.PetStatus;

/**
 * Integration tests for the OpenAPI generated Java Quarkus reactive server and JDK HTTP client
 * code. These tests start a real Grizzly/Jersey server and use the generated reactive (Uni-based)
 * client.
 *
 * <p>Note: The test server uses synchronous JAX-RS endpoints because Grizzly/Jersey doesn't support
 * Mutiny Uni. The reactive client's Uni results are awaited for test assertions.
 */
public class OpenApiIntegrationTest {

  private static final OffsetDateTime TEST_TIME = OffsetDateTime.parse("2024-01-01T12:00:00Z");
  private static final String BASE_URI = "http://127.0.0.1:0";

  private HttpServer server;
  private PetsApiClient client;
  private TestPetsApiServer serverImpl;

  /**
   * Test server implementation using synchronous JAX-RS endpoints. This allows testing the reactive
   * client against a standard Jersey/Grizzly server.
   */
  @Path("/pets")
  public static class TestPetsApiServer {
    private final Pet initialPet;
    private final Map<PetId, Pet> pets = new HashMap<>();

    public TestPetsApiServer() {
      initialPet =
          new Pet(
              Optional.of(List.of("friendly", "cute")),
              new PetId("pet-123"),
              PetStatus.available,
              TEST_TIME,
              Optional.of(Map.of("color", "brown")),
              "Fluffy",
              Optional.empty());
      reset();
    }

    public void reset() {
      pets.clear();
      pets.put(initialPet.id(), initialPet);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createPet(PetCreate body) {
      var newPet =
          new Pet(
              body.tags(),
              new PetId("pet-" + System.nanoTime()),
              body.status().orElse(PetStatus.available),
              TEST_TIME,
              Optional.empty(),
              body.name(),
              Optional.empty());
      pets.put(newPet.id(), newPet);
      return Response.status(201).entity(newPet).build();
    }

    @DELETE
    @Path("/{petId}")
    public void deletePet(@PathParam("petId") PetId petId) {
      pets.remove(petId);
    }

    @GET
    @Path("/{petId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPet(@PathParam("petId") PetId petId) {
      var pet = pets.get(petId);
      if (pet != null) {
        return Response.ok(pet).build();
      } else {
        return Response.status(404)
            .entity(new testapi.model.Error("NOT_FOUND", Optional.empty(), "Pet not found"))
            .build();
      }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Pet> listPets(
        @QueryParam("limit") Optional<Integer> limit,
        @QueryParam("status") Optional<String> status) {
      var filtered = pets.values().stream();
      if (status.isPresent()) {
        filtered = filtered.filter(p -> p.status().value().equals(status.get()));
      }
      var result = filtered.toList();
      if (limit.isPresent()) {
        result = result.subList(0, Math.min(limit.get(), result.size()));
      }
      return result;
    }
  }

  @Before
  public void setUp() {
    serverImpl = new TestPetsApiServer();

    var objectMapper = new ObjectMapper();
    objectMapper.registerModule(new Jdk8Module());
    objectMapper.registerModule(new JavaTimeModule());

    var config =
        new ResourceConfig()
            .registerInstances(serverImpl)
            .register(JacksonFeature.class)
            .register(new ObjectMapperProvider(objectMapper));

    server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config);

    var port = server.getListener("grizzly").getPort();
    var baseUri = URI.create("http://127.0.0.1:" + port);

    // Use HTTP/1.1 - Grizzly doesn't support HTTP/2
    var httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
    client = new PetsApiClient(httpClient, baseUri, objectMapper);
  }

  @After
  public void tearDown() {
    if (server != null) {
      server.shutdownNow();
    }
  }

  @Test
  public void getPetReturnsExistingPet() {
    serverImpl.reset();
    var response = client.getPet(new PetId("pet-123")).await().indefinitely();

    assertTrue(response instanceof Ok);
    var ok = (Ok<Pet, testapi.model.Error>) response;
    assertEquals("Fluffy", ok.value().name());
    assertEquals(new PetId("pet-123"), ok.value().id());
    assertEquals(PetStatus.available, ok.value().status());
  }

  @Test
  public void getPetReturnsNotFoundForNonExistentPet() {
    serverImpl.reset();
    var response = client.getPet(new PetId("nonexistent")).await().indefinitely();

    assertTrue(response instanceof NotFound);
    var notFound = (NotFound<Pet, testapi.model.Error>) response;
    assertEquals("NOT_FOUND", notFound.value().code());
  }

  @Test
  public void createPetCreatesAndReturnsPet() {
    serverImpl.reset();
    var newPet =
        new PetCreate(
            Optional.of(2L),
            Optional.empty(),
            "Buddy",
            Optional.of(PetStatus.pending),
            Optional.of(List.of("playful")),
            Optional.empty());

    var response = client.createPet(newPet).await().indefinitely();

    assertTrue(response instanceof Created);
    var created = (Created<Pet, testapi.model.Error>) response;
    assertEquals("Buddy", created.value().name());
    assertEquals(PetStatus.pending, created.value().status());
    assertEquals(Optional.of(List.of("playful")), created.value().tags());
  }

  @Test
  public void deletePetDeletesExistingPet() {
    serverImpl.reset();
    client.deletePet(new PetId("pet-123")).await().indefinitely();

    var response = client.getPet(new PetId("pet-123")).await().indefinitely();
    assertTrue(response instanceof NotFound);
  }

  @Test
  public void listPetsReturnsAllPets() {
    serverImpl.reset();
    var pets = client.listPets(Optional.empty(), Optional.empty()).await().indefinitely();

    assertFalse(pets.isEmpty());
    assertTrue(pets.stream().anyMatch(p -> p.name().equals("Fluffy")));
  }

  @Test
  public void listPetsWithLimitReturnsLimitedPets() {
    serverImpl.reset();
    var pets = client.listPets(Optional.of(1), Optional.empty()).await().indefinitely();

    assertEquals(1, pets.size());
  }

  /** Provider to use custom ObjectMapper in Jersey */
  @jakarta.ws.rs.ext.Provider
  public static class ObjectMapperProvider
      implements jakarta.ws.rs.ext.ContextResolver<ObjectMapper> {
    private final ObjectMapper objectMapper;

    public ObjectMapperProvider(ObjectMapper objectMapper) {
      this.objectMapper = objectMapper;
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
      return objectMapper;
    }
  }
}
