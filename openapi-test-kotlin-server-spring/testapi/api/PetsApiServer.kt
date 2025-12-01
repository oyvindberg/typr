package testapi.api

import com.fasterxml.jackson.databind.JsonNode
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import java.lang.IllegalStateException
import java.lang.Void
import java.util.Optional
import kotlin.collections.List
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import testapi.api.CreatePetResponse.Status201
import testapi.api.CreatePetResponse.Status400
import testapi.api.DeletePetResponse.Status404
import testapi.api.DeletePetResponse.StatusDefault
import testapi.api.GetPetResponse.Status200
import testapi.model.Pet
import testapi.model.PetCreate

@RestController
@RequestMapping("/pets")
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
@SecurityScheme(name = "apiKeyHeader", type = SecuritySchemeType.APIKEY, `in` = SecuritySchemeIn.HEADER, paramName = "X-API-Key")
@SecurityScheme(name = "apiKeyQuery", type = SecuritySchemeType.APIKEY, `in` = SecuritySchemeIn.QUERY, paramName = "api_key")
@SecurityScheme(name = "oauth2", type = SecuritySchemeType.OAUTH2)
sealed interface PetsApiServer : PetsApi {
  /** Create a pet */
  override fun createPet(body: PetCreate): CreatePetResponse

  /** Endpoint wrapper for createPet - handles response status codes */
  @PostMapping(value = ["/"], consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
  @SecurityRequirement(name = "oauth2", scopes = ["write:pets"])
  @SecurityRequirement(name = "apiKeyHeader")
  fun createPetEndpoint(body: PetCreate): ResponseEntity<*> = when (val __r = createPet(body)) {
    is Status201 -> { val r = __r as Status201; ResponseEntity.ok(r.value) }
    is Status400 -> { val r = __r as Status400; ResponseEntity.status(400).body(r.value) }
    else -> throw IllegalStateException("Unexpected response type")
  }

  /** Delete a pet */
  override fun deletePet(
    /** The pet ID */
    petId: String
  ): DeletePetResponse

  /** Endpoint wrapper for deletePet - handles response status codes */
  @DeleteMapping(value = ["/{petId}"])
  fun deletePetEndpoint(
    /** The pet ID */
    petId: String
  ): ResponseEntity<*> = when (val __r = deletePet(petId)) {
    is Status404 -> { val r = __r as Status404; ResponseEntity.status(404).body(r.value) }
    is StatusDefault -> { val r = __r as StatusDefault; ResponseEntity.status(r.statusCode).body(r.value) }
    else -> throw IllegalStateException("Unexpected response type")
  }

  /** Get a pet by ID */
  override fun getPet(
    /** The pet ID */
    petId: String
  ): GetPetResponse

  /** Endpoint wrapper for getPet - handles response status codes */
  @GetMapping(value = ["/{petId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getPetEndpoint(
    /** The pet ID */
    petId: String
  ): ResponseEntity<*> = when (val __r = getPet(petId)) {
    is Status200 -> { val r = __r as Status200; ResponseEntity.ok(r.value) }
    is testapi.api.GetPetResponse.Status404 -> { val r = __r as testapi.api.GetPetResponse.Status404; ResponseEntity.status(404).body(r.value) }
    else -> throw IllegalStateException("Unexpected response type")
  }

  /** Get pet photo */
  @GetMapping(value = ["/{petId}/photo"], produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
  override fun getPetPhoto(
    /** The pet ID */
    petId: String
  ): Void

  /** List all pets */
  @GetMapping(value = ["/"], produces = [MediaType.APPLICATION_JSON_VALUE])
  override fun listPets(
    /** Maximum number of pets to return */
    limit: Optional<Integer>,
    /** Filter by status */
    status: Optional<String>
  ): List<Pet>

  /** Upload a pet photo */
  @PostMapping(value = ["/{petId}/photo"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
  override fun uploadPetPhoto(
    /** The pet ID */
    petId: String,
    /** Optional caption for the photo */
    caption: String,
    /** The photo file to upload */
    file: Array<Byte>
  ): JsonNode
}