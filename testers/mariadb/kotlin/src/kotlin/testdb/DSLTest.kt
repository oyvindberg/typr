package testdb

import org.junit.Assert.*
import org.junit.Test
import testdb.mariatest_identity.*

/**
 * Tests for the SQL DSL operations.
 */
class DSLTest {
    private val identityRepo = MariatestIdentityRepoImpl()

    @Test
    fun testSelectWithWhere() {
        MariaDbTestHelper.run { c ->
            val row1 = identityRepo.insert(MariatestIdentityRowUnsaved("test_dsl_where"), c)

            val results = identityRepo.select()
                .where { m -> m.id().isEqual(row1.id) }
                .where { m -> m.name().isEqual("test_dsl_where") }
                .toList(c)

            assertEquals(1, results.size)
            assertEquals(row1.id, results[0].id)
            assertEquals("test_dsl_where", results[0].name)
        }
    }

    @Test
    fun testSelectWithOrdering() {
        MariaDbTestHelper.run { c ->
            identityRepo.insert(MariatestIdentityRowUnsaved("DSL_Zulu"), c)
            identityRepo.insert(MariatestIdentityRowUnsaved("DSL_Alpha"), c)
            identityRepo.insert(MariatestIdentityRowUnsaved("DSL_Mike"), c)

            val results = identityRepo.select()
                .where { m -> m.name().isEqual("DSL_Zulu") or m.name().isEqual("DSL_Alpha") or m.name().isEqual("DSL_Mike") }
                .orderBy { m -> m.name().asc() }
                .toList(c)

            assertTrue(results.size >= 3)
            val sorted = results.filter { it.name.startsWith("DSL_") }
            assertEquals("DSL_Alpha", sorted[0].name)
        }
    }

    @Test
    fun testSelectWithOrderByDesc() {
        MariaDbTestHelper.run { c ->
            identityRepo.insert(MariatestIdentityRowUnsaved("DSLDescA"), c)
            identityRepo.insert(MariatestIdentityRowUnsaved("DSLDescB"), c)
            identityRepo.insert(MariatestIdentityRowUnsaved("DSLDescC"), c)

            val results = identityRepo.select()
                .where { m -> m.name().like("DSLDesc%") }
                .orderBy { m -> m.name().desc() }
                .toList(c)

            assertEquals(3, results.size)
            assertEquals("DSLDescC", results[0].name)
            assertEquals("DSLDescB", results[1].name)
            assertEquals("DSLDescA", results[2].name)
        }
    }

    @Test
    fun testSelectWithLimit() {
        MariaDbTestHelper.run { c ->
            for (i in 0 until 10) {
                identityRepo.insert(MariatestIdentityRowUnsaved("Limit$i"), c)
            }

            val results = identityRepo.select()
                .where { m -> m.name().like("Limit%") }
                .limit(3)
                .toList(c)

            assertEquals(3, results.size)
        }
    }

    @Test
    fun testSelectWithOffset() {
        MariaDbTestHelper.run { c ->
            identityRepo.insert(MariatestIdentityRowUnsaved("OffsetA"), c)
            identityRepo.insert(MariatestIdentityRowUnsaved("OffsetB"), c)
            identityRepo.insert(MariatestIdentityRowUnsaved("OffsetC"), c)
            identityRepo.insert(MariatestIdentityRowUnsaved("OffsetD"), c)

            val results = identityRepo.select()
                .where { m -> m.name().like("Offset%") }
                .orderBy { m -> m.name().asc() }
                .offset(2)
                .limit(10)
                .toList(c)

            assertEquals(2, results.size)
            assertEquals("OffsetC", results[0].name)
            assertEquals("OffsetD", results[1].name)
        }
    }

    @Test
    fun testSelectWithCount() {
        MariaDbTestHelper.run { c ->
            identityRepo.insert(MariatestIdentityRowUnsaved("CountA"), c)
            identityRepo.insert(MariatestIdentityRowUnsaved("CountB"), c)
            identityRepo.insert(MariatestIdentityRowUnsaved("CountC"), c)

            val count = identityRepo.select()
                .where { m -> m.name().like("Count%") }
                .count(c)

            assertEquals(3, count)
        }
    }

    @Test
    fun testSelectWithGreaterThan() {
        MariaDbTestHelper.run { c ->
            val row1 = identityRepo.insert(MariatestIdentityRowUnsaved("GT1"), c)
            identityRepo.insert(MariatestIdentityRowUnsaved("GT2"), c)
            identityRepo.insert(MariatestIdentityRowUnsaved("GT3"), c)

            val results = identityRepo.select()
                .where { m -> m.id().greaterThan(row1.id) }
                .where { m -> m.name().like("GT%") }
                .toList(c)

            assertEquals(2, results.size)
        }
    }

    @Test
    fun testSelectWithLike() {
        MariaDbTestHelper.run { c ->
            identityRepo.insert(MariatestIdentityRowUnsaved("LikeTest_ABC"), c)
            identityRepo.insert(MariatestIdentityRowUnsaved("LikeTest_XYZ"), c)
            identityRepo.insert(MariatestIdentityRowUnsaved("OtherName"), c)

            val results = identityRepo.select()
                .where { m -> m.name().like("LikeTest%") }
                .toList(c)

            assertEquals(2, results.size)
        }
    }

    @Test
    fun testSelectWithIn() {
        MariaDbTestHelper.run { c ->
            val row1 = identityRepo.insert(MariatestIdentityRowUnsaved("InTest1"), c)
            identityRepo.insert(MariatestIdentityRowUnsaved("InTest2"), c)
            val row3 = identityRepo.insert(MariatestIdentityRowUnsaved("InTest3"), c)

            val results = identityRepo.select()
                .where { m -> m.id().`in`(row1.id, row3.id) }
                .toList(c)

            assertEquals(2, results.size)
        }
    }

    @Test
    fun testSelectWithProjection() {
        MariaDbTestHelper.run { c ->
            val row = identityRepo.insert(MariatestIdentityRowUnsaved("ProjectionTest"), c)

            val results = identityRepo.select()
                .where { m -> m.id().isEqual(row.id) }
                .map { m -> m.name().tupleWith(m.id()) }
                .toList(c)

            assertEquals(1, results.size)
            assertEquals("ProjectionTest", results[0]._1())
            assertEquals(row.id, results[0]._2())
        }
    }

    @Test
    fun testDeleteWithDSL() {
        MariaDbTestHelper.run { c ->
            val row = identityRepo.insert(MariatestIdentityRowUnsaved("ToDeleteDSL"), c)

            val beforeCount = identityRepo.select()
                .where { m -> m.id().isEqual(row.id) }
                .count(c)
            assertEquals(1, beforeCount)

            identityRepo.deleteById(row.id, c)

            val afterCount = identityRepo.select()
                .where { m -> m.id().isEqual(row.id) }
                .count(c)
            assertEquals(0, afterCount)
        }
    }
}
