package testapi.api

import com.fasterxml.jackson.databind.JsonNode
import java.lang.Exception
import java.util.Optional
import kotlin.collections.List
import testapi.model.Error
import testapi.model.Pet
import testapi.model.PetCreate
import testapi.model.PetId

interface PetsApi {
  /** Create a pet */
  @Throws(Exception::class)
  fun createPet(body: PetCreate): Response201400<Pet, Error>

  /** Delete a pet */
  @Throws(Exception::class)
  fun deletePet(
    /** The pet ID */
    petId: PetId
  ): Unit

  /** Get a pet by ID */
  @Throws(Exception::class)
  fun getPet(
    /** The pet ID */
    petId: PetId
  ): Response200404<Pet, Error>

  /** Get pet photo */
  @Throws(Exception::class)
  fun getPetPhoto(
    /** The pet ID */
    petId: PetId
  ): Unit

  /** List all pets */
  @Throws(Exception::class)
  fun listPets(
    /** Maximum number of pets to return */
    limit: Optional<Integer>,
    /** Filter by status */
    status: Optional<String>
  ): List<Pet>

  /** Upload a pet photo */
  @Throws(Exception::class)
  fun uploadPetPhoto(
    /** The pet ID */
    petId: PetId,
    /** Optional caption for the photo */
    caption: String,
    /** The photo file to upload */
    file: Array<Byte>
  ): JsonNode
}