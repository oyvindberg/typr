package testapi.api;

import static typo.runtime.internal.stringInterpolator.str;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;
import java.util.Optional;
import testapi.model.Error;
import testapi.model.Pet;
import testapi.model.PetCreate;
import testapi.model.PetId;

/** JDK HTTP Client implementation for PetsApi */
public class PetsApiClient implements PetsApi {
  HttpClient httpClient;
  ;

  URI baseUri;
  ;

  ObjectMapper objectMapper;
  ;

  public PetsApiClient(
      /** JDK HTTP client for making HTTP requests */
      HttpClient httpClient,
      /** Base URI for API requests */
      URI baseUri,
      /** Jackson ObjectMapper for JSON serialization */
      ObjectMapper objectMapper) {
    this.httpClient = httpClient;
    this.baseUri = baseUri;
    this.objectMapper = objectMapper;
  }
  ;

  /** Create a pet */
  @Override
  public Uni<Response201400<Pet, Error>> createPet(PetCreate body) {
    try {
      var request =
          HttpRequest.newBuilder(URI.create(baseUri.toString() + "/" + "pets"))
              .method("POST", BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
              .header("Content-Type", "application/json")
              .header("Accept", "application/json")
              .build();
      return Uni.createFrom()
          .completionStage(() -> httpClient.sendAsync(request, BodyHandlers.ofString()))
          .map(
              response -> {
                try {
                  var statusCode = response.statusCode();
                  if (statusCode == 201) {
                    return new Created(objectMapper.readValue(response.body(), Pet.class));
                  } else if (statusCode == 400) {
                    return new BadRequest(objectMapper.readValue(response.body(), Error.class));
                  } else {
                    throw new IllegalStateException(
                        str("Unexpected status code: ", java.lang.String.valueOf(statusCode), ""));
                  }
                } catch (Exception e) {
                  throw new RuntimeException(e);
                }
              });
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  ;

  /** Delete a pet */
  @Override
  public Uni<Void> deletePet(

      /** The pet ID */
      PetId petId) {
    var request =
        HttpRequest.newBuilder(
                URI.create(baseUri.toString() + "/" + "pets" + "/" + petId.toString()))
            .method("DELETE", BodyPublishers.noBody())
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .build();
    return Uni.createFrom()
        .completionStage(() -> httpClient.sendAsync(request, BodyHandlers.ofString()))
        .map(response -> null);
  }
  ;

  /** Get a pet by ID */
  @Override
  public Uni<Response200404<Pet, Error>> getPet(

      /** The pet ID */
      PetId petId) {
    var request =
        HttpRequest.newBuilder(
                URI.create(baseUri.toString() + "/" + "pets" + "/" + petId.toString()))
            .method("GET", BodyPublishers.noBody())
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .build();
    return Uni.createFrom()
        .completionStage(() -> httpClient.sendAsync(request, BodyHandlers.ofString()))
        .map(
            response -> {
              try {
                var statusCode = response.statusCode();
                if (statusCode == 200) {
                  return new Ok(objectMapper.readValue(response.body(), Pet.class));
                } else if (statusCode == 404) {
                  return new NotFound(objectMapper.readValue(response.body(), Error.class));
                } else {
                  throw new IllegalStateException(
                      str("Unexpected status code: ", java.lang.String.valueOf(statusCode), ""));
                }
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            });
  }
  ;

  /** Get pet photo */
  @Override
  public Uni<Void> getPetPhoto(

      /** The pet ID */
      PetId petId) {
    var request =
        HttpRequest.newBuilder(
                URI.create(
                    baseUri.toString() + "/" + "pets" + "/" + petId.toString() + "/" + "photo"))
            .method("GET", BodyPublishers.noBody())
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .build();
    return Uni.createFrom()
        .completionStage(() -> httpClient.sendAsync(request, BodyHandlers.ofString()))
        .map(response -> null);
  }
  ;

  /** List all pets */
  @Override
  public Uni<List<Pet>> listPets(
      /** Maximum number of pets to return */
      Optional<Integer> limit,
      /** Filter by status */
      Optional<String> status) {
    var request =
        HttpRequest.newBuilder(
                URI.create(
                    baseUri.toString()
                        + "/"
                        + "pets"
                        + (limit.isPresent() ? "?limit=" + limit.get().toString() : "")
                        + (status.isPresent() ? "&status=" + status.get().toString() : "")))
            .method("GET", BodyPublishers.noBody())
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .build();
    return Uni.createFrom()
        .completionStage(() -> httpClient.sendAsync(request, BodyHandlers.ofString()))
        .map(
            response -> {
              try {
                return objectMapper.readValue(response.body(), new TypeReference<List<Pet>>() {});
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            });
  }
  ;

  /** Upload a pet photo */
  @Override
  public Uni<JsonNode> uploadPetPhoto(
      /** The pet ID */
      PetId petId,
      /** Optional caption for the photo */
      String caption,
      /** The photo file to upload */
      Byte[] file) {
    var request =
        HttpRequest.newBuilder(
                URI.create(
                    baseUri.toString() + "/" + "pets" + "/" + petId.toString() + "/" + "photo"))
            .method("POST", BodyPublishers.noBody())
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .build();
    return Uni.createFrom()
        .completionStage(() -> httpClient.sendAsync(request, BodyHandlers.ofString()))
        .map(
            response -> {
              try {
                return objectMapper.readValue(response.body(), JsonNode.class);
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            });
  }
  ;
}
