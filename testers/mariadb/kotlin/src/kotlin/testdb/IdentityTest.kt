package testdb

import org.junit.Assert.*
import org.junit.Test
import testdb.mariatest_identity.*

/**
 * Tests for AUTO_INCREMENT (identity) columns in MariaDB.
 */
class IdentityTest {
    private val identityRepo = MariatestIdentityRepoImpl()

    @Test
    fun testInsertWithAutoIncrement() {
        MariaDbTestHelper.run { c ->
            val unsaved = MariatestIdentityRowUnsaved("Test Row 1")
            val inserted = identityRepo.insert(unsaved, c)

            assertNotNull(inserted)
            assertNotNull(inserted.id)
            assertTrue(inserted.id.value > 0)
            assertEquals("Test Row 1", inserted.name)
        }
    }

    @Test
    fun testMultipleInsertsGetUniqueIds() {
        MariaDbTestHelper.run { c ->
            val row1 = identityRepo.insert(MariatestIdentityRowUnsaved("Row A"), c)
            val row2 = identityRepo.insert(MariatestIdentityRowUnsaved("Row B"), c)
            val row3 = identityRepo.insert(MariatestIdentityRowUnsaved("Row C"), c)

            assertNotEquals(row1.id, row2.id)
            assertNotEquals(row2.id, row3.id)
            assertNotEquals(row1.id, row3.id)
        }
    }

    @Test
    fun testSelectByIdWithAutoIncrement() {
        MariaDbTestHelper.run { c ->
            val inserted = identityRepo.insert(MariatestIdentityRowUnsaved("Findable Row"), c)

            val found = identityRepo.selectById(inserted.id, c)
            assertNotNull(found)
            assertEquals(inserted.id, found!!.id)
            assertEquals("Findable Row", found.name)
        }
    }

    @Test
    fun testUpdateWithAutoIncrement() {
        MariaDbTestHelper.run { c ->
            val inserted = identityRepo.insert(MariatestIdentityRowUnsaved("Original"), c)
            val updated = inserted.copy(name = "Updated")

            val wasUpdated = identityRepo.update(updated, c)
            assertTrue(wasUpdated)

            val found = identityRepo.selectById(inserted.id, c)!!
            assertEquals("Updated", found.name)
        }
    }

    @Test
    fun testDeleteWithAutoIncrement() {
        MariaDbTestHelper.run { c ->
            val inserted = identityRepo.insert(MariatestIdentityRowUnsaved("To Delete"), c)

            val deleted = identityRepo.deleteById(inserted.id, c)
            assertTrue(deleted)

            val found = identityRepo.selectById(inserted.id, c)
            assertNull(found)
        }
    }

    @Test
    fun testUpsertWithAutoIncrement() {
        MariaDbTestHelper.run { c ->
            val inserted = identityRepo.insert(MariatestIdentityRowUnsaved("Upsert Test"), c)

            // Upsert existing row
            val toUpsert = inserted.copy(name = "Upserted")
            val upserted = identityRepo.upsert(toUpsert, c)

            assertEquals(inserted.id, upserted.id)
            assertEquals("Upserted", upserted.name)

            // Verify only one row exists
            val all = identityRepo.selectAll(c)
            assertEquals(1, all.filter { it.id == inserted.id }.size)
        }
    }

    @Test
    fun testSelectAll() {
        MariaDbTestHelper.run { c ->
            identityRepo.insert(MariatestIdentityRowUnsaved("All 1"), c)
            identityRepo.insert(MariatestIdentityRowUnsaved("All 2"), c)
            identityRepo.insert(MariatestIdentityRowUnsaved("All 3"), c)

            val all = identityRepo.selectAll(c)
            assertTrue(all.size >= 3)
        }
    }
}
