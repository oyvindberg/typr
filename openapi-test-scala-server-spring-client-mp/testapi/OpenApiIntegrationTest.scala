package testapi

import org.scalatest.funsuite.AnyFunSuite
import testapi.api.*
import testapi.model.{Error, Pet, PetCreate, PetId, PetStatus}
import java.time.OffsetDateTime

/** Unit tests for the OpenAPI generated Scala Spring server code.
  *
  * These tests verify that:
  *   1. The generated server trait can be implemented 2. The generated response types work correctly with phantom type parameters 3. Pattern matching works correctly for response types 4. The codegen
  *      is truly language-agnostic - Scala works with Spring annotations
  */
class OpenApiIntegrationTest extends AnyFunSuite {

  val testTime: OffsetDateTime = OffsetDateTime.parse("2024-01-01T12:00:00Z")

  class TestPetsApiServer extends PetsApiServer {
    private val pets = scala.collection.mutable.Map(
      PetId("pet-123") -> Pet(
        tags = Some(List("friendly", "cute")),
        id = PetId("pet-123"),
        status = PetStatus.available,
        createdAt = testTime,
        metadata = Some(Map("color" -> "brown")),
        name = "Fluffy",
        updatedAt = None
      )
    )

    override def createPet(body: PetCreate): Response201400[Pet, Error] = {
      val newPet = Pet(
        tags = body.tags,
        id = PetId(s"pet-${System.nanoTime()}"),
        status = body.status.getOrElse(PetStatus.available),
        createdAt = testTime,
        metadata = None,
        name = body.name,
        updatedAt = None
      )
      pets(newPet.id) = newPet
      Created(newPet)
    }

    override def deletePet(petId: PetId): Response404Default[Error] = {
      if (pets.contains(petId)) {
        pets.remove(petId)
        Default(204, Error("OK", None, "Deleted"))
      } else {
        NotFound(Error("NOT_FOUND", None, "Pet not found"))
      }
    }

    override def getPet(petId: PetId): Response200404[Pet, Error] = {
      pets.get(petId) match {
        case Some(pet) => Ok(pet)
        case None      => NotFound(Error("NOT_FOUND", None, "Pet not found"))
      }
    }

    override def getPetPhoto(petId: PetId): Void = {
      throw new UnsupportedOperationException("Not implemented")
    }

    override def listPets(limit: Option[Int], status: Option[String]): List[Pet] = {
      var result = pets.values.toList
      status.foreach { s =>
        result = result.filter(_.status.value == s)
      }
      limit.foreach { l =>
        result = result.take(l)
      }
      result
    }

    override def uploadPetPhoto(petId: PetId, caption: String, file: Array[Byte]): io.circe.Json = {
      throw new UnsupportedOperationException("Not implemented")
    }
  }

  test("getPet returns Ok for existing pet") {
    val server = new TestPetsApiServer
    val result = server.getPet(PetId("pet-123"))

    assert(result.isInstanceOf[Ok[?]])
    assert(result.status == "200")

    val ok = result.asInstanceOf[Ok[Pet]]
    assert(ok.value.name == "Fluffy")
  }

  test("getPet returns NotFound for non-existent pet") {
    val server = new TestPetsApiServer
    val result = server.getPet(PetId("nonexistent"))

    assert(result.isInstanceOf[NotFound[?]])
    assert(result.status == "404")

    val notFound = result.asInstanceOf[NotFound[Error]]
    assert(notFound.value.code == "NOT_FOUND")
  }

  test("createPet returns Created") {
    val server = new TestPetsApiServer
    val newPet = PetCreate(
      age = Some(2L),
      email = None,
      name = "Buddy",
      status = Some(PetStatus.pending),
      tags = Some(List("playful")),
      website = None
    )

    val result = server.createPet(newPet)

    assert(result.isInstanceOf[Created[?]])
    assert(result.status == "201")

    val created = result.asInstanceOf[Created[Pet]]
    assert(created.value.name == "Buddy")
    assert(created.value.status == PetStatus.pending)
  }

  test("deletePet returns Default for existing pet") {
    val server = new TestPetsApiServer
    val result = server.deletePet(PetId("pet-123"))

    assert(result.isInstanceOf[Default])
    assert(result.status == "default")

    val default = result.asInstanceOf[Default]
    assert(default.statusCode == 204)
  }

  test("deletePet returns NotFound for missing pet") {
    val server = new TestPetsApiServer
    val result = server.deletePet(PetId("nonexistent"))

    assert(result.isInstanceOf[NotFound[?]])
    assert(result.status == "404")
  }

  test("listPets returns all pets") {
    val server = new TestPetsApiServer
    val result = server.listPets(None, None)

    assert(result.nonEmpty)
    assert(result.exists(_.name == "Fluffy"))
  }

  test("pattern matching works for response types") {
    val server = new TestPetsApiServer

    // Test Response200404 pattern matching
    val getPetResult = server.getPet(PetId("pet-123"))
    val resultBody = getPetResult match {
      case ok: Ok[Pet @unchecked]               => s"OK: ${ok.value.name}"
      case notFound: NotFound[Error @unchecked] => s"NotFound: ${notFound.value.code}"
    }
    assert(resultBody == "OK: Fluffy")

    // Test Response201400 pattern matching
    val createResult = server.createPet(PetCreate(None, None, "Test", None, None, None))
    val createBody = createResult match {
      case created: Created[Pet @unchecked]         => s"Created: ${created.value.name}"
      case badRequest: BadRequest[Error @unchecked] => s"BadRequest: ${badRequest.value.code}"
    }
    assert(createBody == "Created: Test")

    // Test Response404Default pattern matching
    val deleteResult = server.deletePet(PetId("nonexistent"))
    val deleteBody = deleteResult match {
      case notFound: NotFound[Error @unchecked] => s"NotFound: ${notFound.value.code}"
      case default: Default                     => s"Default: ${default.statusCode}"
    }
    assert(deleteBody == "NotFound: NOT_FOUND")
  }

  test("phantom type parameters enable polymorphism") {
    // Created[Pet] should be assignable to Response201400[Pet, Error]
    val response: Response201400[Pet, Error] = Created(
      Pet(None, PetId("test-id"), PetStatus.available, OffsetDateTime.now(), None, "Test", None)
    )
    assert(response.status == "201")

    // Ok[Pet] should be assignable to Response200404[Pet, Error]
    val okResponse: Response200404[Pet, Error] = Ok(
      Pet(None, PetId("test-id"), PetStatus.available, OffsetDateTime.now(), None, "Test", None)
    )
    assert(okResponse.status == "200")

    // NotFound[Error] should be assignable to Response200404[Pet, Error]
    val notFoundResponse: Response200404[Pet, Error] = NotFound(
      Error("NOT_FOUND", None, "Not found")
    )
    assert(notFoundResponse.status == "404")
  }
}
