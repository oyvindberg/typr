package testapi.api

import io.circe.Json
import java.lang.Exception
import java.util.Optional
import testapi.model.Error
import testapi.model.Pet
import testapi.model.PetCreate
import testapi.model.PetId

trait PetsApi {
  /** Create a pet */
  @throws[Exception]
  def createPet(body: PetCreate): Response201400[Pet, Error]

  /** Delete a pet */
  @throws[Exception]
  def deletePet(
    /** The pet ID */
    petId: PetId
  ): Unit

  /** Get a pet by ID */
  @throws[Exception]
  def getPet(
    /** The pet ID */
    petId: PetId
  ): Response200404[Pet, Error]

  /** Get pet photo */
  @throws[Exception]
  def getPetPhoto(
    /** The pet ID */
    petId: PetId
  ): Unit

  /** List all pets */
  @throws[Exception]
  def listPets(
    /** Maximum number of pets to return */
    limit: Optional[Integer],
    /** Filter by status */
    status: Optional[String]
  ): java.util.List[Pet]

  /** Upload a pet photo */
  @throws[Exception]
  def uploadPetPhoto(
    /** The pet ID */
    petId: PetId,
    /** Optional caption for the photo */
    caption: String,
    /** The photo file to upload */
    file: Array[Byte]
  ): Json
}