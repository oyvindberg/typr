package testapi.api

import java.lang.Exception
import kotlin.collections.List
import testapi.model.Animal

interface AnimalsApi {
  /** List all animals (polymorphic) */
  @Throws(Exception::class)
  fun listAnimals(): Response2004XX5XX<List<Animal>>
}