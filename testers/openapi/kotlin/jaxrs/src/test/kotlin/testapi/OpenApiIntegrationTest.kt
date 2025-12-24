package testapi

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ContextResolver
import jakarta.ws.rs.ext.Provider
import org.glassfish.grizzly.http.server.HttpServer
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory
import org.glassfish.jersey.jackson.JacksonFeature
import org.glassfish.jersey.server.ResourceConfig
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testapi.api.*
import testapi.model.Error
import testapi.model.Pet
import testapi.model.PetCreate
import testapi.model.PetId
import testapi.model.PetStatus
import java.net.URI
import java.net.http.HttpClient
import java.time.OffsetDateTime

/**
 * Integration tests for the OpenAPI generated Kotlin JAX-RS server and JDK HTTP client code.
 * These tests start a real Grizzly/Jersey server and use the generated client.
 */
class OpenApiIntegrationTest {

    private lateinit var server: HttpServer
    private lateinit var client: PetsApiClient
    private lateinit var serverImpl: TestPetsApiServer

    /**
     * Test server implementation using synchronous JAX-RS endpoints.
     * @Path annotation required because Jersey doesn't inherit class-level annotations from interfaces.
     */
    @Path("/pets")
    class TestPetsApiServer {
        private val initialPet = Pet(
            tags = listOf("friendly", "cute"),
            id = PetId("pet-123"),
            status = PetStatus.available,
            createdAt = TEST_TIME,
            metadata = mapOf("color" to "brown"),
            name = "Fluffy",
            updatedAt = null
        )
        private val pets = mutableMapOf<PetId, Pet>()

        init {
            reset()
        }

        fun reset() {
            pets.clear()
            pets[initialPet.id] = initialPet
        }

        @POST
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        fun createPet(body: PetCreate): Response {
            val newPet = Pet(
                tags = body.tags,
                id = PetId("pet-${System.nanoTime()}"),
                status = body.status ?: PetStatus.available,
                createdAt = TEST_TIME,
                metadata = null,
                name = body.name,
                updatedAt = null
            )
            pets[newPet.id] = newPet
            return Response.status(201).entity(newPet).build()
        }

        @DELETE
        @Path("/{petId}")
        fun deletePet(@PathParam("petId") petId: PetId) {
            pets.remove(petId)
        }

        @GET
        @Path("/{petId}")
        @Produces(MediaType.APPLICATION_JSON)
        fun getPet(@PathParam("petId") petId: PetId): Response {
            val pet = pets[petId]
            return if (pet != null) {
                Response.ok(pet).build()
            } else {
                Response.status(404).entity(Error(code = "NOT_FOUND", details = null, message = "Pet not found")).build()
            }
        }

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        fun listPets(@QueryParam("limit") limit: Int?, @QueryParam("status") status: String?): List<Pet> {
            var filtered = pets.values.toList()
            if (status != null) {
                filtered = filtered.filter { it.status.value == status }
            }
            if (limit != null) {
                filtered = filtered.take(limit)
            }
            return filtered
        }
    }

    @BeforeEach
    fun setUp() {
        serverImpl = TestPetsApiServer()

        val objectMapper = ObjectMapper()
        objectMapper.registerModule(Jdk8Module())
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.registerModule(KotlinModule.Builder().build())

        val config = ResourceConfig()
            .registerInstances(serverImpl)
            .register(JacksonFeature::class.java)
            .register(ObjectMapperProvider(objectMapper))

        server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config)

        val port = server.getListener("grizzly").port
        val baseUri = URI.create("http://127.0.0.1:$port")

        // Use HTTP/1.1 - Grizzly doesn't support HTTP/2
        val httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build()
        client = PetsApiClient(
            httpClient = httpClient,
            baseUri = baseUri,
            objectMapper = objectMapper
        )
    }

    @AfterEach
    fun tearDown() {
        server.shutdownNow()
    }

    @Test
    fun getPetReturnsExistingPet() {
        serverImpl.reset()
        val response = client.getPet(PetId("pet-123"))

        assertTrue(response is Ok<*>)
        val ok = response as Ok<Pet>
        assertEquals("Fluffy", ok.value.name)
        assertEquals(PetId("pet-123"), ok.value.id)
        assertEquals(PetStatus.available, ok.value.status)
    }

    @Test
    fun getPetReturnsNotFoundForNonExistentPet() {
        serverImpl.reset()
        val response = client.getPet(PetId("nonexistent"))

        assertTrue(response is NotFound<*>)
        val notFound = response as NotFound<Error>
        assertEquals("NOT_FOUND", notFound.value.code)
    }

    @Test
    fun createPetCreatesAndReturnsPet() {
        serverImpl.reset()
        val newPet = PetCreate(
            age = 2L,
            email = null,
            name = "Buddy",
            status = PetStatus.pending,
            tags = listOf("playful"),
            website = null
        )

        val response = client.createPet(newPet)

        assertTrue(response is Created<*>)
        val created = response as Created<Pet>
        assertEquals("Buddy", created.value.name)
        assertEquals(PetStatus.pending, created.value.status)
        assertEquals(listOf("playful"), created.value.tags)
    }

    @Test
    fun deletePetDeletesExistingPet() {
        serverImpl.reset()
        client.deletePet(PetId("pet-123"))

        val response = client.getPet(PetId("pet-123"))
        assertTrue(response is NotFound<*>)
    }

    @Test
    fun listPetsReturnsAllPets() {
        serverImpl.reset()
        val pets = client.listPets(null, null)

        assertFalse(pets.isEmpty())
        assertTrue(pets.any { it.name == "Fluffy" })
    }

    @Test
    fun listPetsWithLimitReturnsLimitedPets() {
        serverImpl.reset()
        val pets = client.listPets(1, null)

        assertEquals(1, pets.size)
    }

    /** Provider to use custom ObjectMapper in Jersey */
    @Provider
    class ObjectMapperProvider(private val objectMapper: ObjectMapper) : ContextResolver<ObjectMapper> {
        override fun getContext(type: Class<*>?): ObjectMapper = objectMapper
    }

    companion object {
        private val TEST_TIME: OffsetDateTime = OffsetDateTime.parse("2024-01-01T12:00:00Z")
        private const val BASE_URI = "http://127.0.0.1:0"
    }
}
