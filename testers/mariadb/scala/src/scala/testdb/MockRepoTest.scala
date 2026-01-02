package testdb

import org.scalatest.funsuite.AnyFunSuite
import testdb.mariatest_identity.*

import java.util.concurrent.atomic.AtomicInteger

class MockRepoTest extends AnyFunSuite {

  test("mockInsert") {
    val idCounter = new AtomicInteger(1)
    val mockRepo = new MariatestIdentityRepoMock(unsaved => unsaved.toRow(MariatestIdentityId(idCounter.getAndIncrement())))

    val row1 = mockRepo.insert(MariatestIdentityRowUnsaved("Row 1"))(using null)
    val row2 = mockRepo.insert(MariatestIdentityRowUnsaved("Row 2"))(using null)

    val _ = assert(row1.id.value >= 0)
    val _ = assert(row2.id.value >= 0)
    val _ = assert(row1.id != row2.id)
    val _ = assert(row1.name == "Row 1")
    assert(row2.name == "Row 2")
  }

  test("mockSelectById") {
    val idCounter = new AtomicInteger(1)
    val mockRepo = new MariatestIdentityRepoMock(unsaved => unsaved.toRow(MariatestIdentityId(idCounter.getAndIncrement())))

    val inserted = mockRepo.insert(MariatestIdentityRowUnsaved("Test Row"))(using null)

    val found = mockRepo.selectById(inserted.id)(using null)
    val _ = assert(found.isDefined)
    val _ = assert(found.get.id == inserted.id)
    assert(found.get.name == "Test Row")
  }

  test("mockSelectByIdNotFound") {
    val idCounter = new AtomicInteger(1)
    val mockRepo = new MariatestIdentityRepoMock(unsaved => unsaved.toRow(MariatestIdentityId(idCounter.getAndIncrement())))

    val found = mockRepo.selectById(MariatestIdentityId(999))(using null)
    assert(found.isEmpty)
  }

  test("mockSelectAll") {
    val idCounter = new AtomicInteger(1)
    val mockRepo = new MariatestIdentityRepoMock(unsaved => unsaved.toRow(MariatestIdentityId(idCounter.getAndIncrement())))

    val _ = mockRepo.insert(MariatestIdentityRowUnsaved("Row A"))(using null)
    val _ = mockRepo.insert(MariatestIdentityRowUnsaved("Row B"))(using null)
    val _ = mockRepo.insert(MariatestIdentityRowUnsaved("Row C"))(using null)

    val all = mockRepo.selectAll(using null)
    assert(all.size == 3)
  }

  test("mockUpdate") {
    val idCounter = new AtomicInteger(1)
    val mockRepo = new MariatestIdentityRepoMock(unsaved => unsaved.toRow(MariatestIdentityId(idCounter.getAndIncrement())))

    val inserted = mockRepo.insert(MariatestIdentityRowUnsaved("Original"))(using null)
    val updated = inserted.copy(name = "Updated")

    val wasUpdated = mockRepo.update(updated)(using null)
    val _ = assert(wasUpdated)

    val found = mockRepo.selectById(inserted.id)(using null).get
    assert(found.name == "Updated")
  }

  test("mockUpdateNotFound") {
    val idCounter = new AtomicInteger(1)
    val mockRepo = new MariatestIdentityRepoMock(unsaved => unsaved.toRow(MariatestIdentityId(idCounter.getAndIncrement())))

    val nonExistent = MariatestIdentityRow(MariatestIdentityId(999), "Test")
    val wasUpdated = mockRepo.update(nonExistent)(using null)
    assert(!wasUpdated)
  }

  test("mockDelete") {
    val idCounter = new AtomicInteger(1)
    val mockRepo = new MariatestIdentityRepoMock(unsaved => unsaved.toRow(MariatestIdentityId(idCounter.getAndIncrement())))

    val inserted = mockRepo.insert(MariatestIdentityRowUnsaved("To Delete"))(using null)

    val deleted = mockRepo.deleteById(inserted.id)(using null)
    val _ = assert(deleted)

    val found = mockRepo.selectById(inserted.id)(using null)
    assert(found.isEmpty)
  }

  test("mockDeleteNotFound") {
    val idCounter = new AtomicInteger(1)
    val mockRepo = new MariatestIdentityRepoMock(unsaved => unsaved.toRow(MariatestIdentityId(idCounter.getAndIncrement())))

    val deleted = mockRepo.deleteById(MariatestIdentityId(999))(using null)
    assert(!deleted)
  }

  test("mockUpsertInsert") {
    val idCounter = new AtomicInteger(1)
    val mockRepo = new MariatestIdentityRepoMock(unsaved => unsaved.toRow(MariatestIdentityId(idCounter.getAndIncrement())))

    val newRow = MariatestIdentityRow(MariatestIdentityId(100), "New Row")
    val upserted = mockRepo.upsert(newRow)(using null)

    val _ = assert(upserted.id == MariatestIdentityId(100))
    val _ = assert(upserted.name == "New Row")

    val found = mockRepo.selectById(MariatestIdentityId(100))(using null)
    assert(found.isDefined)
  }

  test("mockUpsertUpdate") {
    val idCounter = new AtomicInteger(1)
    val mockRepo = new MariatestIdentityRepoMock(unsaved => unsaved.toRow(MariatestIdentityId(idCounter.getAndIncrement())))

    val inserted = mockRepo.insert(MariatestIdentityRowUnsaved("Original"))(using null)
    val toUpsert = inserted.copy(name = "Upserted")
    val upserted = mockRepo.upsert(toUpsert)(using null)

    val _ = assert(upserted.id == inserted.id)
    val _ = assert(upserted.name == "Upserted")
    assert(mockRepo.selectAll(using null).size == 1)
  }

  test("mockWithInterfacePolymorphism") {
    val idCounter = new AtomicInteger(1)

    val repo: MariatestIdentityRepo = new MariatestIdentityRepoMock(unsaved => unsaved.toRow(MariatestIdentityId(idCounter.getAndIncrement())))

    val inserted = repo.insert(MariatestIdentityRowUnsaved("Test"))(using null)
    val found = repo.selectById(inserted.id)(using null)

    val _ = assert(found.isDefined)
    assert(found.get.name == "Test")
  }
}
