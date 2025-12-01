package testapi.api



sealed interface AnimalsApi {
  /** List all animals (polymorphic) */
  fun listAnimals(): ListAnimalsResponse
}