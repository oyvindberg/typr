package testdb

import dev.typr.foundations.data.Json
import dev.typr.foundations.data.Uint1
import dev.typr.foundations.data.Xml
import org.junit.Assert.*
import org.junit.Test
import testdb.all_scalar_types.*
import testdb.customtypes.Defaulted
import java.math.BigDecimal
import java.time.*
import java.util.UUID

/**
 * Comprehensive tests for all SQL Server scalar data types.
 * Tests the all_scalar_types table which has 38 columns covering all supported SQL Server types.
 */
class AllTypesTest {
    private val repo = AllScalarTypesRepoImpl()

    companion object {
        fun createSampleRow(): AllScalarTypesRowUnsaved = AllScalarTypesRowUnsaved(
            colTinyint = Uint1(255),
            colSmallint = 1000.toShort(),
            colInt = 100000,
            colBigint = 10000000000L,
            colDecimal = BigDecimal("12345.6789"),
            colNumeric = BigDecimal("999.99"),
            colMoney = BigDecimal("922337203685477.5807"),
            colSmallmoney = BigDecimal("214748.3647"),
            colReal = 3.14f,
            colFloat = 2.718281828,
            colBit = true,
            colChar = "char_val  ",
            colVarchar = "varchar_value",
            colVarcharMax = "varchar max content",
            colText = "text content",
            colNchar = "nchar_val ",
            colNvarchar = "nvarchar_value 中文",
            colNvarcharMax = "nvarchar max content 日本語",
            colNtext = "ntext content",
            colBinary = null,
            colVarbinary = byteArrayOf(1, 2, 3),
            colVarbinaryMax = byteArrayOf(4, 5, 6, 7, 8),
            colImage = byteArrayOf(9, 10, 11),
            colDate = LocalDate.of(2025, 1, 15),
            colTime = LocalTime.of(14, 30, 45, 123456700),
            colDatetime = LocalDateTime.of(2025, 1, 15, 14, 30, 45),
            colSmalldatetime = LocalDateTime.of(2025, 1, 15, 14, 30),
            colDatetime2 = LocalDateTime.of(2025, 1, 15, 14, 30, 45, 123456700),
            colDatetimeoffset = OffsetDateTime.of(2025, 1, 15, 14, 30, 45, 0, ZoneOffset.ofHours(-5)),
            colUniqueidentifier = UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
            colXml = Xml("<root><element>value</element></root>"),
            colJson = Json("""{"key": "value"}"""),
            colHierarchyid = null,
            colGeography = null,
            colGeometry = null,
            colNotNull = Defaulted.Provided("test_value")
        )
    }

    @Test
    fun testInsertAndSelectAllTypes() {
        SqlServerTestHelper.run { c ->
            val row = createSampleRow()
            val inserted = repo.insert(row, c)

            assertNotNull(inserted)
            assertNotNull(inserted.id)
            assertEquals(row.colTinyint, inserted.colTinyint)
            assertEquals(row.colSmallint, inserted.colSmallint)
            assertEquals(row.colInt, inserted.colInt)
            assertEquals(row.colBigint, inserted.colBigint)
            assertEquals(row.colBit, inserted.colBit)
            assertEquals(row.colVarchar, inserted.colVarchar)
            assertEquals(row.colNvarchar, inserted.colNvarchar)
            assertEquals(row.colDate, inserted.colDate)
            assertEquals(row.colUniqueidentifier, inserted.colUniqueidentifier)

            assertNotNull(inserted.colRowversion)

            val found = repo.selectById(inserted.id, c)
            assertNotNull(found)
            assertEquals(inserted.id, found!!.id)
        }
    }

    @Test
    fun testUpdateAllTypes() {
        SqlServerTestHelper.run { c ->
            val row = createSampleRow()
            val inserted = repo.insert(row, c)

            val updated = inserted.copy(
                colVarchar = "updated_varchar",
                colNvarchar = "updated_nvarchar",
                colDecimal = BigDecimal("9999.9999"),
                colBit = false
            )

            val wasUpdated = repo.update(updated, c)
            assertTrue(wasUpdated)

            val found = repo.selectById(inserted.id, c)!!
            assertEquals("updated_varchar", found.colVarchar)
            assertEquals("updated_nvarchar", found.colNvarchar)
            assertEquals(BigDecimal("9999.9999"), found.colDecimal)
            assertEquals(false, found.colBit)
        }
    }

    @Test
    fun testDeleteAllTypes() {
        SqlServerTestHelper.run { c ->
            val row = createSampleRow()
            val inserted = repo.insert(row, c)

            val deleted = repo.deleteById(inserted.id, c)
            assertTrue(deleted)

            val found = repo.selectById(inserted.id, c)
            assertNull(found)
        }
    }

    @Test
    fun testInsertWithNulls() {
        SqlServerTestHelper.run { c ->
            val row = AllScalarTypesRowUnsaved()
            val inserted = repo.insert(row, c)

            assertNotNull(inserted)
            assertNotNull(inserted.id)

            assertNull(inserted.colTinyint)
            assertNull(inserted.colSmallint)
            assertNull(inserted.colInt)
            assertNull(inserted.colBit)
            assertNull(inserted.colVarchar)
            assertNull(inserted.colDate)

            assertEquals("default_value", inserted.colNotNull)

            assertNotNull(inserted.colRowversion)
        }
    }

    @Test
    fun testIntegerTypes() {
        SqlServerTestHelper.run { c ->
            val row = createSampleRow()
            val inserted = repo.insert(row, c)

            assertEquals(255.toShort(), inserted.colTinyint!!.value)
            assertEquals(1000.toShort(), inserted.colSmallint)
            assertEquals(100000, inserted.colInt)
            assertEquals(10000000000L, inserted.colBigint)
        }
    }

    @Test
    fun testMoneyTypes() {
        SqlServerTestHelper.run { c ->
            val row = createSampleRow()
            val inserted = repo.insert(row, c)

            assertEquals(BigDecimal("922337203685477.5807"), inserted.colMoney)
            assertEquals(BigDecimal("214748.3647"), inserted.colSmallmoney)
        }
    }

    @Test
    fun testDateTimeTypes() {
        SqlServerTestHelper.run { c ->
            val row = createSampleRow()
            val inserted = repo.insert(row, c)

            assertEquals(LocalDate.of(2025, 1, 15), inserted.colDate)
            assertNotNull(inserted.colTime)
            assertNotNull(inserted.colDatetime)
            assertNotNull(inserted.colDatetime2)
            assertNotNull(inserted.colDatetimeoffset)
        }
    }

    @Test
    fun testUuidType() {
        SqlServerTestHelper.run { c ->
            val uuid = UUID.randomUUID()
            val row = createSampleRow().copy(colUniqueidentifier = uuid)
            val inserted = repo.insert(row, c)

            assertEquals(uuid, inserted.colUniqueidentifier)
        }
    }

    @Test
    fun testXmlType() {
        SqlServerTestHelper.run { c ->
            val xml = Xml("<person><name>John</name><age>30</age></person>")
            val row = createSampleRow().copy(colXml = xml)
            val inserted = repo.insert(row, c)

            assertNotNull(inserted.colXml)
            assertTrue(inserted.colXml!!.value.contains("John"))
        }
    }

    @Test
    fun testJsonType() {
        SqlServerTestHelper.run { c ->
            val json = Json("""{"name": "test", "values": [1, 2, 3]}""")
            val row = createSampleRow().copy(colJson = json)
            val inserted = repo.insert(row, c)

            assertNotNull(inserted.colJson)
            assertTrue(inserted.colJson!!.value.contains("name"))
        }
    }

    @Test
    fun testBinaryTypes() {
        SqlServerTestHelper.run { c ->
            val binaryData = byteArrayOf(0x00, 0x01, 0x02, 0xFF.toByte(), 0xFE.toByte())
            val row = createSampleRow().copy(colVarbinary = binaryData)
            val inserted = repo.insert(row, c)

            assertNotNull(inserted.colVarbinary)
            assertArrayEquals(binaryData, inserted.colVarbinary)
        }
    }

    @Test
    fun testUnicodeStrings() {
        SqlServerTestHelper.run { c ->
            val unicode = "Hello 世界 🌍 مرحبا"
            val row = createSampleRow().copy(colNvarchar = unicode)
            val inserted = repo.insert(row, c)

            assertEquals(unicode, inserted.colNvarchar)
        }
    }

    @Test
    fun testRowversionAutoGenerated() {
        SqlServerTestHelper.run { c ->
            val row1 = repo.insert(createSampleRow(), c)
            val row2 = repo.insert(createSampleRow(), c)

            assertNotNull(row1.colRowversion)
            assertNotNull(row2.colRowversion)
            assertFalse(row1.colRowversion.contentEquals(row2.colRowversion))
        }
    }
}
