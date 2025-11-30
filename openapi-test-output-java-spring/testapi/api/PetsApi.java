package testapi.api;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import java.lang.Void;
import java.util.List;
import java.util.Optional;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import testapi.model.Pet;
import testapi.model.PetCreate;

@RestController
@RequestMapping("/pets")
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
@SecurityScheme(name = "apiKeyHeader", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER, paramName = "X-API-Key")
@SecurityScheme(name = "apiKeyQuery", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.QUERY, paramName = "api_key")
@SecurityScheme(name = "oauth2", type = SecuritySchemeType.OAUTH2)
public sealed interface PetsApi {
  @PostMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @SecurityRequirement(name = "oauth2", scopes = { "write:pets" })
  @SecurityRequirement(name = "apiKeyHeader")
  /** Create a pet */
  CreatePetResponse createPet(@RequestBody PetCreate body);

  @DeleteMapping(value = "/{petId}")
  /** Delete a pet */
  Void deletePet(
  
    /** The pet ID */
    @PathVariable("petId") String petId
  );

  @GetMapping(value = "/{petId}", produces = MediaType.APPLICATION_JSON_VALUE)
  /** Get a pet by ID */
  GetPetResponse getPet(
  
    /** The pet ID */
    @PathVariable("petId") String petId
  );

  @GetMapping(value = "/{petId}/photo", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  /** Get pet photo */
  Void getPetPhoto(
  
    /** The pet ID */
    @PathVariable("petId") String petId
  );

  @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
  /** List all pets */
  List<Pet> listPets(
    /** Maximum number of pets to return */
    @RequestParam(name = "limit", required = false, defaultValue = "20") Optional<Integer> limit,
    /** Filter by status */
    @RequestParam(name = "status", required = false, defaultValue = "available") Optional<String> status
  );

  @PostMapping(value = "/{petId}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  /** Upload a pet photo */
  JsonNode uploadPetPhoto(
    /** The pet ID */
    @PathVariable("petId") String petId,
    /** Optional caption for the photo */
    @RequestPart(name = "caption", required = false) String caption,
    /** The photo file to upload */
    @RequestPart(name = "file") MultipartFile file
  );
}