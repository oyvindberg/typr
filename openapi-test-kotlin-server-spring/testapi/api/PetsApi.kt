package testapi.api

import com.fasterxml.jackson.databind.JsonNode
import java.lang.Void
import java.util.Optional
import kotlin.collections.List
import testapi.model.Pet
import testapi.model.PetCreate

sealed interface PetsApi {
  /** Create a pet */
  fun createPet(body: PetCreate): CreatePetResponse

  /** Delete a pet */
  fun deletePet(
    /** The pet ID */
    petId: String
  ): DeletePetResponse

  /** Get a pet by ID */
  fun getPet(
    /** The pet ID */
    petId: String
  ): GetPetResponse

  /** Get pet photo */
  fun getPetPhoto(
    /** The pet ID */
    petId: String
  ): Void

  /** List all pets */
  fun listPets(
    /** Maximum number of pets to return */
    limit: Optional<Integer>,
    /** Filter by status */
    status: Optional<String>
  ): List<Pet>

  /** Upload a pet photo */
  fun uploadPetPhoto(
    /** The pet ID */
    petId: String,
    /** Optional caption for the photo */
    caption: String,
    /** The photo file to upload */
    file: Array<Byte>
  ): JsonNode
}