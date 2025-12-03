package testapi

import com.fasterxml.jackson.databind.JsonNode
import org.junit.Test
import org.junit.Assert.*
import testapi.api.*
import testapi.model.Error
import testapi.model.Pet
import testapi.model.PetCreate
import testapi.model.PetId
import testapi.model.PetStatus
import java.time.OffsetDateTime
import java.util.Optional

/**
 * Unit tests for the OpenAPI generated Kotlin JAX-RS server code.
 *
 * These tests verify that:
 * 1. The generated server interface can be implemented
 * 2. The generated response types work correctly with phantom type parameters
 * 3. The when expressions work correctly for pattern matching
 */
class OpenApiIntegrationTest {

    private val testTime = OffsetDateTime.parse("2024-01-01T12:00:00Z")

    inner class TestPetsApiServer : PetsApiServer {
        private val pets = mutableMapOf(
            PetId("pet-123") to Pet(
                tags = Optional.of(listOf("friendly", "cute")),
                id = PetId("pet-123"),
                status = PetStatus.available,
                createdAt = testTime,
                metadata = Optional.of(mapOf("color" to "brown")),
                name = "Fluffy",
                updatedAt = Optional.empty()
            )
        )

        override fun createPet(body: PetCreate): Response201400<Pet, Error> {
            val newPet = Pet(
                tags = body.tags,
                id = PetId("pet-${System.nanoTime()}"),
                status = body.status.orElse(PetStatus.available),
                createdAt = testTime,
                metadata = Optional.empty(),
                name = body.name,
                updatedAt = Optional.empty()
            )
            pets[newPet.id] = newPet
            return Created(newPet)
        }

        override fun deletePet(petId: PetId): Response404Default<Error> {
            return if (pets.containsKey(petId)) {
                pets.remove(petId)
                Default(204, Error("OK", Optional.empty(), "Deleted"))
            } else {
                NotFound(Error("NOT_FOUND", Optional.empty(), "Pet not found"))
            }
        }

        override fun getPet(petId: PetId): Response200404<Pet, Error> {
            return pets[petId]?.let { Ok(it) }
                ?: NotFound(Error("NOT_FOUND", Optional.empty(), "Pet not found"))
        }

        override fun getPetPhoto(petId: PetId): Void {
            throw UnsupportedOperationException("Not implemented")
        }

        override fun listPets(limit: Optional<Integer>, status: Optional<String>): List<Pet> {
            var result = pets.values.toList()
            status.ifPresent { s ->
                result = result.filter { it.status.value == s }
            }
            limit.ifPresent { l ->
                result = result.take(l.toInt())
            }
            return result
        }

        override fun uploadPetPhoto(petId: PetId, caption: String, file: Array<Byte>): JsonNode {
            throw UnsupportedOperationException("Not implemented")
        }
    }

    @Test
    fun testGetPetReturnsOkForExistingPet() {
        val server = TestPetsApiServer()
        val result = server.getPet(PetId("pet-123"))

        assertTrue(result is Ok<*>)
        assertEquals("200", result.status())

        val ok = result as Ok<Pet>
        assertEquals("Fluffy", ok.value.name)
    }

    @Test
    fun testGetPetReturnsNotFoundForNonExistentPet() {
        val server = TestPetsApiServer()
        val result = server.getPet(PetId("nonexistent"))

        assertTrue(result is NotFound<*>)
        assertEquals("404", result.status())

        val notFound = result as NotFound<Error>
        assertEquals("NOT_FOUND", notFound.value.code)
    }

    @Test
    fun testCreatePetReturnsCreated() {
        val server = TestPetsApiServer()
        val newPet = PetCreate(
            age = Optional.of(2L),
            email = Optional.empty(),
            name = "Buddy",
            status = Optional.of(PetStatus.pending),
            tags = Optional.of(listOf("playful")),
            website = Optional.empty()
        )

        val result = server.createPet(newPet)

        assertTrue(result is Created<*>)
        assertEquals("201", result.status())

        val created = result as Created<Pet>
        assertEquals("Buddy", created.value.name)
        assertEquals(PetStatus.pending, created.value.status)
    }

    @Test
    fun testDeletePetReturnsDefaultForExistingPet() {
        val server = TestPetsApiServer()
        val result = server.deletePet(PetId("pet-123"))

        assertTrue(result is Default)
        assertEquals("default", result.status())

        val default = result as Default
        assertEquals(204, default.statusCode)
    }

    @Test
    fun testDeletePetReturnsNotFoundForMissingPet() {
        val server = TestPetsApiServer()
        val result = server.deletePet(PetId("nonexistent"))

        assertTrue(result is NotFound<*>)
        assertEquals("404", result.status())
    }

    @Test
    fun testListPetsReturnsList() {
        val server = TestPetsApiServer()
        val result = server.listPets(Optional.empty(), Optional.empty())

        assertTrue(result.isNotEmpty())
        assertTrue(result.any { pet -> pet.name == "Fluffy" })
    }

    @Test
    fun testWhenExpressionWorksForResponseTypes() {
        val server = TestPetsApiServer()

        // Test Response200404 pattern matching
        val getPetResult = server.getPet(PetId("pet-123"))
        val resultBody = when (getPetResult) {
            is Ok<*> -> "OK: ${(getPetResult as Ok<Pet>).value.name}"
            is NotFound<*> -> "NotFound: ${(getPetResult as NotFound<Error>).value.code}"
            else -> "Unknown"
        }
        assertEquals("OK: Fluffy", resultBody)

        // Test Response201400 pattern matching
        val createResult = server.createPet(
            PetCreate(Optional.empty(), Optional.empty(), "Test", Optional.empty(), Optional.empty(), Optional.empty())
        )
        val createBody = when (createResult) {
            is Created<*> -> "Created: ${(createResult as Created<Pet>).value.name}"
            is BadRequest<*> -> "BadRequest: ${(createResult as BadRequest<Error>).value.code}"
            else -> "Unknown"
        }
        assertEquals("Created: Test", createBody)

        // Test Response404Default pattern matching
        val deleteResult = server.deletePet(PetId("nonexistent"))
        val deleteBody = when (deleteResult) {
            is NotFound<*> -> "NotFound: ${(deleteResult as NotFound<Error>).value.code}"
            is Default -> "Default: ${deleteResult.statusCode}"
            else -> "Unknown"
        }
        assertEquals("NotFound: NOT_FOUND", deleteBody)
    }

    @Test
    fun testPhantomTypeParametersEnablePolymorphism() {
        // Created<Pet> should be assignable to Response201400<Pet, Error>
        val response: Response201400<Pet, Error> = Created(
            Pet(
                Optional.empty(), PetId("test-id"), PetStatus.available,
                OffsetDateTime.now(), Optional.empty(), "Test", Optional.empty()
            )
        )
        assertEquals("201", response.status())

        // Ok<Pet> should be assignable to Response200404<Pet, Error>
        val okResponse: Response200404<Pet, Error> = Ok(
            Pet(
                Optional.empty(), PetId("test-id"), PetStatus.available,
                OffsetDateTime.now(), Optional.empty(), "Test", Optional.empty()
            )
        )
        assertEquals("200", okResponse.status())

        // NotFound<Error> should be assignable to Response200404<Pet, Error>
        val notFoundResponse: Response200404<Pet, Error> = NotFound(
            Error("NOT_FOUND", Optional.empty(), "Not found")
        )
        assertEquals("404", notFoundResponse.status())
    }
}
