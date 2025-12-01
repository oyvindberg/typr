package testapi.api

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityScheme
import java.lang.IllegalStateException
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import testapi.api.ListAnimalsResponse.Status200
import testapi.api.ListAnimalsResponse.Status4XX
import testapi.api.ListAnimalsResponse.Status5XX

@RestController
@RequestMapping("/animals")
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
@SecurityScheme(name = "apiKeyHeader", type = SecuritySchemeType.APIKEY, `in` = SecuritySchemeIn.HEADER, paramName = "X-API-Key")
@SecurityScheme(name = "apiKeyQuery", type = SecuritySchemeType.APIKEY, `in` = SecuritySchemeIn.QUERY, paramName = "api_key")
@SecurityScheme(name = "oauth2", type = SecuritySchemeType.OAUTH2)
sealed interface AnimalsApiServer : AnimalsApi {
  /** List all animals (polymorphic) */
  override fun listAnimals(): ListAnimalsResponse

  /** Endpoint wrapper for listAnimals - handles response status codes */
  @GetMapping(value = ["/"], produces = [MediaType.APPLICATION_JSON_VALUE])
  fun listAnimalsEndpoint(): ResponseEntity<*> = when (val __r = listAnimals()) {
    is Status200 -> { val r = __r as Status200; ResponseEntity.ok(r.value) }
    is Status4XX -> { val r = __r as Status4XX; ResponseEntity.status(r.statusCode).body(r.value) }
    is Status5XX -> { val r = __r as Status5XX; ResponseEntity.status(r.statusCode).body(r.value) }
    else -> throw IllegalStateException("Unexpected response type")
  }
}