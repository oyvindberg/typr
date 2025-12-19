package testapi;

import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import testapi.api.*;
import testapi.model.Pet;
import testapi.model.PetCreate;
import testapi.model.PetId;
import testapi.model.PetStatus;

/**
 * Integration tests for the OpenAPI generated Java Spring server and JDK HTTP client code. These
 * tests start a real Spring Boot server and use the generated client.
 */
public class OpenApiIntegrationTest {

  private static final OffsetDateTime TEST_TIME = OffsetDateTime.parse("2024-01-01T12:00:00Z");

  private ConfigurableApplicationContext context;
  private PetsApiClient client;
  private TestPetsApiServer serverImpl;

  /** Test server implementation */
  public static class TestPetsApiServer implements PetsApiServer {
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

    @Override
    public Response201400<Pet, testapi.model.Error> createPet(PetCreate body) {
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
      return new Created<>(newPet);
    }

    @Override
    public Void deletePet(PetId petId) {
      pets.remove(petId);
      return null;
    }

    @Override
    public Response200404<Pet, testapi.model.Error> getPet(PetId petId) {
      var pet = pets.get(petId);
      if (pet != null) {
        return new Ok<>(pet);
      } else {
        return new NotFound<>(
            new testapi.model.Error("NOT_FOUND", Optional.empty(), "Pet not found"));
      }
    }

    @Override
    public Void getPetPhoto(PetId petId) {
      throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<Pet> listPets(Optional<Integer> limit, Optional<String> status) {
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

    @Override
    public JsonNode uploadPetPhoto(PetId petId, String caption, Byte[] file) {
      throw new UnsupportedOperationException("Not implemented");
    }
  }

  /** Spring Boot test application */
  @SpringBootApplication
  public static class TestApplication {
    private static TestPetsApiServer testServer;

    public static void setTestServer(TestPetsApiServer server) {
      testServer = server;
    }

    @Bean
    public PetsApiServer petsApiServer() {
      return testServer;
    }

    @Bean
    public ObjectMapper objectMapper() {
      var mapper = new ObjectMapper();
      mapper.registerModule(new Jdk8Module());
      mapper.registerModule(new JavaTimeModule());
      return mapper;
    }
  }

  @Before
  public void setUp() {
    serverImpl = new TestPetsApiServer();
    TestApplication.setTestServer(serverImpl);

    // Start Spring Boot with random port
    context = SpringApplication.run(TestApplication.class, "--server.port=0");

    var port = context.getEnvironment().getProperty("local.server.port", Integer.class);
    var baseUri = URI.create("http://127.0.0.1:" + port);

    var objectMapper = new ObjectMapper();
    objectMapper.registerModule(new Jdk8Module());
    objectMapper.registerModule(new JavaTimeModule());

    var httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

    client = new PetsApiClient(httpClient, baseUri, objectMapper);
  }

  @After
  public void tearDown() {
    if (context != null) {
      context.close();
    }
  }

  @Test
  public void getPetReturnsExistingPet() throws Exception {
    serverImpl.reset();
    var response = client.getPet(new PetId("pet-123"));

    assertTrue(response instanceof Ok);
    var ok = (Ok<Pet, testapi.model.Error>) response;
    assertEquals("Fluffy", ok.value().name());
    assertEquals(new PetId("pet-123"), ok.value().id());
    assertEquals(PetStatus.available, ok.value().status());
  }

  @Test
  public void getPetReturnsNotFoundForNonExistentPet() throws Exception {
    serverImpl.reset();
    var response = client.getPet(new PetId("nonexistent"));

    assertTrue(response instanceof NotFound);
    var notFound = (NotFound<Pet, testapi.model.Error>) response;
    assertEquals("NOT_FOUND", notFound.value().code());
  }

  @Test
  public void createPetCreatesAndReturnsPet() throws Exception {
    serverImpl.reset();
    var newPet =
        new PetCreate(
            Optional.of(2L),
            Optional.empty(),
            "Buddy",
            Optional.of(PetStatus.pending),
            Optional.of(List.of("playful")),
            Optional.empty());

    var response = client.createPet(newPet);

    assertTrue(response instanceof Created);
    var created = (Created<Pet, testapi.model.Error>) response;
    assertEquals("Buddy", created.value().name());
    assertEquals(PetStatus.pending, created.value().status());
    assertEquals(Optional.of(List.of("playful")), created.value().tags());
  }

  @Test
  public void deletePetDeletesExistingPet() throws Exception {
    serverImpl.reset();
    client.deletePet(new PetId("pet-123"));

    var response = client.getPet(new PetId("pet-123"));
    assertTrue(response instanceof NotFound);
  }

  @Test
  public void listPetsReturnsAllPets() throws Exception {
    serverImpl.reset();
    var pets = client.listPets(Optional.empty(), Optional.empty());

    assertFalse(pets.isEmpty());
    assertTrue(pets.stream().anyMatch(p -> p.name().equals("Fluffy")));
  }

  @Test
  public void listPetsWithLimitReturnsLimitedPets() throws Exception {
    serverImpl.reset();
    var pets = client.listPets(Optional.of(1), Optional.empty());

    assertEquals(1, pets.size());
  }
}
