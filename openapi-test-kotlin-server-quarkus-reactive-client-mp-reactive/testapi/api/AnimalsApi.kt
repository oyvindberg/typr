package testapi.api

import io.smallrye.mutiny.Uni

sealed interface AnimalsApi {
  /** List all animals (polymorphic) */
  fun listAnimals(): Uni<ListAnimalsResponse>
}