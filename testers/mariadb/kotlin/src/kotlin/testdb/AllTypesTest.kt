package testdb

import dev.typr.foundations.data.Json
import dev.typr.foundations.data.Uint1
import dev.typr.foundations.data.Uint2
import dev.typr.foundations.data.Uint4
import dev.typr.foundations.data.Uint8
import dev.typr.foundations.data.maria.Inet4
import dev.typr.foundations.data.maria.Inet6
import dev.typr.foundations.data.maria.MariaSet
import org.junit.Assert.*
import org.junit.Test
import testdb.customtypes.Defaulted
import testdb.mariatest.*
import testdb.mariatestnull.*
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Year

/**
 * Comprehensive tests for all MariaDB data types. Tests the mariatest (all NOT NULL columns) and
 * mariatestnull (all nullable columns) tables.
 */
class AllTypesTest {
    private val mariatestRepo = MariatestRepoImpl()
    private val mariatestnullRepo = MariatestnullRepoImpl()

    /** Create a sample row with all types populated */
    private fun createSampleRow(id: Int): MariatestRow {
        return MariatestRow(
            tinyintCol = 127.toByte(),
            smallintCol = 32767.toShort(),
            mediumintCol = 8388607,
            intCol = MariatestId(id),
            bigintCol = 9223372036854775807L,
            tinyintUCol = Uint1.of(255),
            smallintUCol = Uint2.of(65535),
            mediumintUCol = Uint4.of(16777215),
            intUCol = Uint4.of(4294967295L),
            bigintUCol = Uint8.of(BigInteger("18446744073709551615")),
            decimalCol = BigDecimal("12345.67"),
            numericCol = BigDecimal("9876.5432"),
            floatCol = 3.14f,
            doubleCol = 2.718281828,
            boolCol = true,
            bitCol = byteArrayOf(0xFF.toByte()),
            bit1Col = byteArrayOf(0x01.toByte()),
            charCol = "char_val  ",
            varcharCol = "varchar_value",
            tinytextCol = "tinytext",
            textCol = "text content",
            mediumtextCol = "mediumtext content",
            longtextCol = "longtext content",
            binaryCol = byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15),
            varbinaryCol = byteArrayOf(1, 2, 3),
            tinyblobCol = byteArrayOf(4, 5, 6),
            blobCol = byteArrayOf(7, 8, 9),
            mediumblobCol = byteArrayOf(10, 11, 12),
            longblobCol = byteArrayOf(13, 14, 15),
            dateCol = LocalDate.of(2025, 1, 15),
            timeCol = LocalTime.of(14, 30, 45),
            timeFspCol = LocalTime.of(14, 30, 45, 123456000),
            datetimeCol = LocalDateTime.of(2025, 1, 15, 14, 30, 45),
            datetimeFspCol = LocalDateTime.of(2025, 1, 15, 14, 30, 45, 123456000),
            timestampCol = LocalDateTime.now(),
            timestampFspCol = LocalDateTime.now(),
            yearCol = Year.of(2025),
            setCol = MariaSet.fromString("x,y"),
            jsonCol = Json("""{"key": "value"}"""),
            inet4Col = Inet4("192.168.1.1"),
            inet6Col = Inet6("::1")
        )
    }

    @Test
    fun testInsertAndSelectAllTypes() {
        MariaDbTestHelper.run { c ->
            val row = createSampleRow(1)
            val inserted = mariatestRepo.insert(row, c)

            assertNotNull(inserted)
            assertEquals(row.intCol, inserted.intCol)
            assertEquals(row.tinyintCol, inserted.tinyintCol)
            assertEquals(row.smallintCol, inserted.smallintCol)
            assertEquals(row.bigintCol, inserted.bigintCol)
            assertEquals(row.decimalCol, inserted.decimalCol)
            assertEquals(row.boolCol, inserted.boolCol)
            assertEquals(row.varcharCol, inserted.varcharCol)
            assertEquals(row.dateCol, inserted.dateCol)
            assertEquals(row.yearCol, inserted.yearCol)
            assertEquals(row.inet4Col, inserted.inet4Col)
            assertEquals(row.inet6Col, inserted.inet6Col)

            // Verify we can select it back
            val found = mariatestRepo.selectById(inserted.intCol, c)
            assertNotNull(found)
            assertEquals(inserted.intCol, found!!.intCol)
        }
    }

    @Test
    fun testUpdateAllTypes() {
        MariaDbTestHelper.run { c ->
            val row = createSampleRow(2)
            val inserted = mariatestRepo.insert(row, c)

            // Update some fields
            val updated = inserted.copy(
                varcharCol = "updated_varchar",
                decimalCol = BigDecimal("999.99"),
                boolCol = false
            )

            val wasUpdated = mariatestRepo.update(updated, c)
            assertTrue(wasUpdated)

            val found = mariatestRepo.selectById(inserted.intCol, c)!!
            assertEquals("updated_varchar", found.varcharCol)
            assertEquals(BigDecimal("999.99"), found.decimalCol)
            assertEquals(false, found.boolCol)
        }
    }

    @Test
    fun testDeleteAllTypes() {
        MariaDbTestHelper.run { c ->
            val row = createSampleRow(3)
            val inserted = mariatestRepo.insert(row, c)

            val deleted = mariatestRepo.deleteById(inserted.intCol, c)
            assertTrue(deleted)

            val found = mariatestRepo.selectById(inserted.intCol, c)
            assertNull(found)
        }
    }

    @Test
    fun testSelectAll() {
        MariaDbTestHelper.run { c ->
            val row1 = createSampleRow(4)
            val row2 = createSampleRow(5)

            mariatestRepo.insert(row1, c)
            mariatestRepo.insert(row2, c)

            val all = mariatestRepo.selectAll(c)
            assertTrue(all.size >= 2)
        }
    }

    // ==================== Nullable Types Tests ====================

    @Test
    fun testInsertNullableWithAllNulls() {
        MariaDbTestHelper.run { c ->
            // Use the no-arg constructor that sets all fields to UseDefault
            val unsaved = MariatestnullRowUnsaved()

            val inserted = mariatestnullRepo.insert(unsaved, c)
            assertNotNull(inserted)

            // Verify all fields are null
            assertNull(inserted.tinyintCol)
            assertNull(inserted.smallintCol)
            assertNull(inserted.intCol)
            assertNull(inserted.bigintCol)
            assertNull(inserted.decimalCol)
            assertNull(inserted.boolCol)
            assertNull(inserted.varcharCol)
            assertNull(inserted.dateCol)
            assertNull(inserted.inet4Col)
            assertNull(inserted.inet6Col)
        }
    }

    @Test
    fun testInsertNullableWithValues() {
        MariaDbTestHelper.run { c ->
            // Explicitly test ALL 42 nullable columns with real values - use Row type directly
            val row = MariatestnullRow(
                tinyintCol = 42.toByte(),
                smallintCol = 1000.toShort(),
                mediumintCol = 50000,
                intCol = 100000,
                bigintCol = 1234567890L,
                tinyintUCol = Uint1.of(200),
                smallintUCol = Uint2.of(40000),
                mediumintUCol = Uint4.of(8000000),
                intUCol = Uint4.of(3000000000L),
                bigintUCol = Uint8.of(BigInteger("12345678901234567890")),
                decimalCol = BigDecimal("123.45"),
                numericCol = BigDecimal("678.90"),
                floatCol = 1.5f,
                doubleCol = 2.5,
                boolCol = true,
                bitCol = byteArrayOf(0xAB.toByte()),
                bit1Col = byteArrayOf(0x01.toByte()),
                charCol = "char      ",
                varcharCol = "varchar",
                tinytextCol = "tinytext",
                textCol = "text",
                mediumtextCol = "mediumtext",
                longtextCol = "longtext",
                binaryCol = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16),
                varbinaryCol = byteArrayOf(1, 2, 3),
                tinyblobCol = byteArrayOf(4, 5, 6),
                blobCol = byteArrayOf(7, 8, 9),
                mediumblobCol = byteArrayOf(10, 11, 12),
                longblobCol = byteArrayOf(13, 14, 15),
                dateCol = LocalDate.of(2025, 6, 15),
                timeCol = LocalTime.of(10, 30),
                timeFspCol = LocalTime.of(10, 30, 45, 500000000),
                datetimeCol = LocalDateTime.of(2025, 6, 15, 10, 30),
                datetimeFspCol = LocalDateTime.of(2025, 6, 15, 10, 30, 45, 500000000),
                timestampCol = LocalDateTime.now(),
                timestampFspCol = LocalDateTime.now(),
                yearCol = Year.of(2024),
                setCol = MariaSet.fromString("y,z"),
                jsonCol = Json("""{"test": true}"""),
                inet4Col = Inet4("10.0.0.1"),
                inet6Col = Inet6("fe80::1")
            )

            val inserted = mariatestnullRepo.insert(row, c)
            assertNotNull(inserted)

            // Verify some fields
            assertEquals(42.toByte(), inserted.tinyintCol)
            assertEquals(1000.toShort(), inserted.smallintCol)
            assertEquals(BigDecimal("123.45"), inserted.decimalCol)
            assertEquals(true, inserted.boolCol)
            assertEquals("varchar", inserted.varcharCol)
            assertEquals(LocalDate.of(2025, 6, 15), inserted.dateCol)
            assertEquals(Year.of(2024), inserted.yearCol)
            assertEquals(Inet4("10.0.0.1"), inserted.inet4Col)
        }
    }

    // ==================== Individual Type Tests ====================

    @Test
    fun testIntegerTypes() {
        MariaDbTestHelper.run { c ->
            val row = createSampleRow(10)
            val inserted = mariatestRepo.insert(row, c)

            assertEquals(127.toByte(), inserted.tinyintCol)
            assertEquals(32767.toShort(), inserted.smallintCol)
            assertEquals(8388607, inserted.mediumintCol)
            assertEquals(9223372036854775807L, inserted.bigintCol)
        }
    }

    @Test
    fun testUnsignedTypes() {
        MariaDbTestHelper.run { c ->
            val row = createSampleRow(11)
            val inserted = mariatestRepo.insert(row, c)

            assertEquals(Uint1.of(255), inserted.tinyintUCol)
            assertEquals(Uint2.of(65535), inserted.smallintUCol)
            assertEquals(Uint4.of(16777215), inserted.mediumintUCol)
            assertEquals(Uint4.of(4294967295L), inserted.intUCol)
            assertEquals(Uint8.of(BigInteger("18446744073709551615")), inserted.bigintUCol)
        }
    }

    @Test
    fun testDecimalTypes() {
        MariaDbTestHelper.run { c ->
            val row = createSampleRow(12)
            val inserted = mariatestRepo.insert(row, c)

            assertEquals(BigDecimal("12345.67"), inserted.decimalCol)
            assertEquals(BigDecimal("9876.5432"), inserted.numericCol)
        }
    }

    @Test
    fun testDateTimeTypes() {
        MariaDbTestHelper.run { c ->
            val row = createSampleRow(13)
            val inserted = mariatestRepo.insert(row, c)

            assertEquals(LocalDate.of(2025, 1, 15), inserted.dateCol)
            assertEquals(LocalTime.of(14, 30, 45), inserted.timeCol)
            assertEquals(Year.of(2025), inserted.yearCol)
            assertNotNull(inserted.timestampCol)
        }
    }

    @Test
    fun testSetType() {
        MariaDbTestHelper.run { c ->
            // Test various set combinations
            val row1 = createSampleRow(20).copy(setCol = MariaSet.fromString("x"))
            val inserted1 = mariatestRepo.insert(row1, c)
            assertEquals(MariaSet.fromString("x"), inserted1.setCol)

            val row2 = createSampleRow(21).copy(setCol = MariaSet.fromString("x,y,z"))
            val inserted2 = mariatestRepo.insert(row2, c)
            assertEquals(MariaSet.fromString("x,y,z"), inserted2.setCol)
        }
    }

    @Test
    fun testInetTypes() {
        MariaDbTestHelper.run { c ->
            val row = createSampleRow(30).copy(
                inet4Col = Inet4("192.168.0.1"),
                inet6Col = Inet6("2001:db8::1")
            )
            val inserted = mariatestRepo.insert(row, c)

            assertEquals(Inet4("192.168.0.1"), inserted.inet4Col)
            assertEquals(Inet6("2001:db8::1"), inserted.inet6Col)
        }
    }

    @Test
    fun testJsonType() {
        MariaDbTestHelper.run { c ->
            val jsonValue = Json("""{"name": "test", "values": [1, 2, 3]}""")
            val row = createSampleRow(40).copy(jsonCol = jsonValue)
            val inserted = mariatestRepo.insert(row, c)

            assertNotNull(inserted.jsonCol)
            assertTrue(inserted.jsonCol.value().contains("name"))
        }
    }
}
