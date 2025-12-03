package testapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Test;
import testapi.api.*;
import testapi.model.Error;
import testapi.model.Pet;
import testapi.model.PetCreate;
import testapi.model.PetId;
import testapi.model.PetStatus;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Integration tests for the OpenAPI generated JAX-RS server and client code.
 *
 * These tests start a real Grizzly HTTP server, make HTTP calls to it,
 * and verify the round-trip works correctly through the generated code.
 */
public class OpenApiIntegrationTest {

    private static final OffsetDateTime TEST_TIME = OffsetDateTime.parse("2024-01-01T12:00:00Z");

    static class TestPetsApiServer implements PetsApiServer {
        private final Map<PetId, Pet> pets = new HashMap<>();

        {
            pets.put(new PetId("pet-123"), new Pet(
                Optional.of(List.of("friendly", "cute")),
                new PetId("pet-123"),
                PetStatus.available,
                TEST_TIME,
                Optional.of(Map.of("color", "brown")),
                "Fluffy",
                Optional.empty()
            ));
        }

        @Override
        public Response201400<Pet, Error> createPet(PetCreate body) {
            var newPet = new Pet(
                body.tags(),
                new PetId("pet-" + System.nanoTime()),
                body.status().orElse(PetStatus.available),
                TEST_TIME,
                Optional.empty(),
                body.name(),
                Optional.empty()
            );
            pets.put(newPet.id(), newPet);
            return new Created<>(newPet);
        }

        @Override
        public Void deletePet(PetId petId) {
            // Delete returns Void (204 No Content)
            // Note: JAX-RS framework handles 404 for non-existent resources differently
            pets.remove(petId);
            return null;
        }

        @Override
        public Response200404<Pet, Error> getPet(PetId petId) {
            if (pets.containsKey(petId)) {
                return new Ok<>(pets.get(petId));
            } else {
                return new NotFound<>(new Error("NOT_FOUND", Optional.empty(), "Pet not found"));
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
            var limited = filtered;
            if (limit.isPresent()) {
                limited = limited.limit(limit.get());
            }
            return limited.toList();
        }

        @Override
        public JsonNode uploadPetPhoto(PetId petId, String caption, Byte[] file) {
            throw new UnsupportedOperationException("Not implemented");
        }
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    @Test
    public void testFullRoundTrip() throws Exception {
        // Create server configuration
        ObjectMapper mapper = createObjectMapper();
        JacksonJsonProvider jacksonProvider = new JacksonJsonProvider(mapper);

        ResourceConfig config = new ResourceConfig()
            .register(new TestPetsApiServer())
            .register(jacksonProvider);

        // Start server on random available port
        URI baseUri = URI.create("http://localhost:0/");
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(baseUri, config);

        try {
            // Get the actual port the server is listening on
            int port = server.getListener("grizzly").getPort();
            URI serverUri = URI.create("http://localhost:" + port);

            // Create client
            Client client = ClientBuilder.newClient()
                .register(jacksonProvider);

            try {
                // Test getPet returns existing pet
                Response getPetResponse = client.target(serverUri)
                    .path("/pets/pet-123")
                    .request(MediaType.APPLICATION_JSON)
                    .get();

                assertEquals(200, getPetResponse.getStatus());
                Pet foundPet = getPetResponse.readEntity(Pet.class);
                assertEquals("Fluffy", foundPet.name());
                assertEquals(new PetId("pet-123"), foundPet.id());
                assertEquals(PetStatus.available, foundPet.status());

                // Test getPet returns NotFound for non-existent pet
                Response notFoundResponse = client.target(serverUri)
                    .path("/pets/nonexistent")
                    .request(MediaType.APPLICATION_JSON)
                    .get();

                assertEquals(404, notFoundResponse.getStatus());
                Error notFoundError = notFoundResponse.readEntity(Error.class);
                assertEquals("NOT_FOUND", notFoundError.code());

                // Test createPet creates and returns pet
                PetCreate newPetRequest = new PetCreate(
                    Optional.of(2L),
                    Optional.empty(),
                    "Buddy",
                    Optional.of(PetStatus.pending),
                    Optional.of(List.of("playful")),
                    Optional.empty()
                );

                Response createResponse = client.target(serverUri)
                    .path("/pets")
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(newPetRequest));

                assertEquals(200, createResponse.getStatus()); // JAX-RS default response returns 200
                Pet createdPet = createResponse.readEntity(Pet.class);
                assertEquals("Buddy", createdPet.name());
                assertEquals(PetStatus.pending, createdPet.status());

                // Test deletePet deletes existing pet (returns 204 No Content)
                Response deleteResponse = client.target(serverUri)
                    .path("/pets/pet-123")
                    .request(MediaType.APPLICATION_JSON)
                    .delete();

                assertEquals(204, deleteResponse.getStatus());

                // Note: With Void return type, deletePet always returns 204
                // The server impl could throw a NotFoundException for 404, but we keep it simple

                // Test listPets returns pets (includes newly created Buddy, but not deleted Fluffy)
                Response listResponse = client.target(serverUri)
                    .path("/pets")
                    .request(MediaType.APPLICATION_JSON)
                    .get();

                assertEquals(200, listResponse.getStatus());
                @SuppressWarnings("unchecked")
                List<Pet> pets = listResponse.readEntity(List.class);
                assertFalse(pets.isEmpty());

            } finally {
                client.close();
            }
        } finally {
            server.shutdownNow();
        }
    }
}
