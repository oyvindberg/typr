package testapi.api

import java.lang.Exception
import testapi.model.Animal

trait AnimalsApi {

  /** List all animals (polymorphic) */
  @throws[Exception]
  def listAnimals: Response2004XX5XX[java.util.List[Animal]]
}
