package testapi.api

import com.fasterxml.jackson.databind.JsonNode
import io.smallrye.mutiny.Uni
import kotlin.collections.List
import testapi.model.Error
import testapi.model.Pet
import testapi.model.PetCreate
import testapi.model.PetId

interface PetsApi {
  /** Create a pet */
  abstract fun createPet(body: PetCreate): Uni<Response201400<Pet, Error>>

  /** Delete a pet */
  abstract fun deletePet(
    /** The pet ID */
    petId: PetId
  ): Uni<Unit>

  /** Get a pet by ID */
  abstract fun getPet(
    /** The pet ID */
    petId: PetId
  ): Uni<Response200404<Pet, Error>>

  /** Get pet photo */
  abstract fun getPetPhoto(
    /** The pet ID */
    petId: PetId
  ): Uni<Unit>

  /** List all pets */
  abstract fun listPets(
    /** Maximum number of pets to return */
    limit: Int?,
    /** Filter by status */
    status: String?
  ): Uni<List<Pet>>

  /** Upload a pet photo */
  abstract fun uploadPetPhoto(
    /** The pet ID */
    petId: PetId,
    /** Optional caption for the photo */
    caption: String,
    /** The photo file to upload */
    file: Array<Byte>
  ): Uni<JsonNode>
}