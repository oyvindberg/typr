package testapi.api

import java.lang.IllegalStateException
import kotlin.collections.List
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import testapi.api.Response2004XX5XX.Status200
import testapi.api.Response2004XX5XX.Status4XX
import testapi.api.Response2004XX5XX.Status5XX
import testapi.model.Animal

interface AnimalsApiServer : AnimalsApi {
  /** List all animals (polymorphic) */
  override fun listAnimals(): Response2004XX5XX<List<Animal>>

  /** Endpoint wrapper for listAnimals - handles response status codes */
  @GetMapping(value = ["/"], produces = [MediaType.APPLICATION_JSON_VALUE])
  fun listAnimalsEndpoint(): ResponseEntity<*> = when (val __r = listAnimals()) {
    is Status200 -> { val r = __r as Status200; ResponseEntity.ok(r.value) }
    is Status4XX -> { val r = __r as Status4XX; ResponseEntity.status(r.statusCode).body(r.value) }
    is Status5XX -> { val r = __r as Status5XX; ResponseEntity.status(r.statusCode).body(r.value) }
    else -> throw IllegalStateException("Unexpected response type")
  }
}