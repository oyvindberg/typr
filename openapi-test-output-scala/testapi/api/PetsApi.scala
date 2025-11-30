package testapi.api

import java.lang.Void
import testapi.model.Pet
import testapi.model.PetCreate

sealed trait PetsApi {
  /** Create a pet */
  def createPet(body: PetCreate): CreatePetResponse

  /** Delete a pet */
  def deletePet(
    /** The pet ID */
    petId: String
  ): Void

  /** Get a pet by ID */
  def getPet(
    /** The pet ID */
    petId: String
  ): GetPetResponse

  /** List all pets */
  def listPets(
    /** Maximum number of pets to return */
    limit: Option[Int],
    /** Filter by status */
    status: Option[String]
  ): List[Pet]
}