package testdb;

import static org.junit.Assert.*;

import dev.typr.foundations.data.Json;
import dev.typr.foundations.data.Uint1;
import dev.typr.foundations.data.Xml;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.Test;
import testdb.all_scalar_types.*;
import testdb.customtypes.Defaulted;

/**
 * Comprehensive tests for all SQL Server scalar data types. Tests the all_scalar_types table which
 * has 38 columns covering all supported SQL Server types.
 */
public class AllTypesTest {
  private final AllScalarTypesRepoImpl repo = new AllScalarTypesRepoImpl();

  /** Create a sample unsaved row with common types populated */
  static AllScalarTypesRowUnsaved createSampleRow() {
    return new AllScalarTypesRowUnsaved(
        Optional.of(Uint1.of(255)), // tinyint (0-255, stored as Uint1)
        Optional.of((short) 1000), // smallint
        Optional.of(100000), // int
        Optional.of(10000000000L), // bigint
        Optional.of(new BigDecimal("12345.6789")), // decimal(18,4)
        Optional.of(new BigDecimal("999.99")), // numeric(10,2)
        Optional.of(new BigDecimal("922337203685477.5807")), // money
        Optional.of(new BigDecimal("214748.3647")), // smallmoney
        Optional.of(3.14f), // real
        Optional.of(2.718281828), // float
        Optional.of(true), // bit
        Optional.of("char_val  "), // char(10)
        Optional.of("varchar_value"), // varchar(255)
        Optional.of("varchar max content"), // varchar(max)
        Optional.of("text content"), // text
        Optional.of("nchar_val "), // nchar(10)
        Optional.of("nvarchar_value 中文"), // nvarchar(255)
        Optional.of("nvarchar max content 日本語"), // nvarchar(max)
        Optional.of("ntext content"), // ntext
        Optional.of(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10}), // binary(10)
        Optional.of(new byte[] {1, 2, 3}), // varbinary(255)
        Optional.of(new byte[] {4, 5, 6, 7, 8}), // varbinary(max)
        Optional.of(new byte[] {9, 10, 11}), // image
        Optional.of(LocalDate.of(2025, 1, 15)), // date
        Optional.of(LocalTime.of(14, 30, 45, 123456700)), // time(7)
        Optional.of(LocalDateTime.of(2025, 1, 15, 14, 30, 45)), // datetime
        Optional.of(LocalDateTime.of(2025, 1, 15, 14, 30)), // smalldatetime
        Optional.of(LocalDateTime.of(2025, 1, 15, 14, 30, 45, 123456700)), // datetime2(7)
        Optional.of(
            OffsetDateTime.of(
                2025, 1, 15, 14, 30, 45, 0, ZoneOffset.ofHours(-5))), // datetimeoffset(7)
        Optional.of(UUID.fromString("550e8400-e29b-41d4-a716-446655440000")), // uniqueidentifier
        Optional.of(new Xml("<root><element>value</element></root>")), // xml
        Optional.of(new Json("{\"key\": \"value\"}")), // json (nvarchar with check)
        Optional.empty(), // hierarchyid (skip for now)
        Optional.empty(), // geography (skip for now)
        Optional.empty(), // geometry (skip for now)
        new Defaulted.Provided<>("test_value")); // col_not_null
  }

  @Test
  public void testInsertAndSelectAllTypes() {
    SqlServerTestHelper.run(
        c -> {
          var row = createSampleRow();
          var inserted = repo.insert(row, c);

          assertNotNull(inserted);
          assertNotNull(inserted.id());
          assertEquals(row.colTinyint(), inserted.colTinyint());
          assertEquals(row.colSmallint(), inserted.colSmallint());
          assertEquals(row.colInt(), inserted.colInt());
          assertEquals(row.colBigint(), inserted.colBigint());
          assertEquals(row.colBit(), inserted.colBit());
          assertEquals(row.colVarchar(), inserted.colVarchar());
          assertEquals(row.colNvarchar(), inserted.colNvarchar());
          assertEquals(row.colDate(), inserted.colDate());
          assertEquals(row.colUniqueidentifier(), inserted.colUniqueidentifier());

          // ROWVERSION should be auto-generated
          assertNotNull(inserted.colRowversion());

          // Verify we can select it back
          var found = repo.selectById(inserted.id(), c);
          assertTrue(found.isPresent());
          assertEquals(inserted.id(), found.get().id());
        });
  }

  @Test
  public void testUpdateAllTypes() {
    SqlServerTestHelper.run(
        c -> {
          var row = createSampleRow();
          var inserted = repo.insert(row, c);

          // Update some fields
          var updated =
              inserted
                  .withColVarchar(Optional.of("updated_varchar"))
                  .withColNvarchar(Optional.of("updated_nvarchar"))
                  .withColDecimal(Optional.of(new BigDecimal("9999.9999")))
                  .withColBit(Optional.of(false));

          boolean wasUpdated = repo.update(updated, c);
          assertTrue(wasUpdated);

          var found = repo.selectById(inserted.id(), c).orElseThrow();
          assertEquals(Optional.of("updated_varchar"), found.colVarchar());
          assertEquals(Optional.of("updated_nvarchar"), found.colNvarchar());
          assertEquals(Optional.of(new BigDecimal("9999.9999")), found.colDecimal());
          assertEquals(Optional.of(false), found.colBit());
        });
  }

  @Test
  public void testDeleteAllTypes() {
    SqlServerTestHelper.run(
        c -> {
          var row = createSampleRow();
          var inserted = repo.insert(row, c);

          boolean deleted = repo.deleteById(inserted.id(), c);
          assertTrue(deleted);

          var found = repo.selectById(inserted.id(), c);
          assertFalse(found.isPresent());
        });
  }

  @Test
  public void testInsertWithNulls() {
    SqlServerTestHelper.run(
        c -> {
          // Use the no-arg constructor that sets all to empty/default
          var row = new AllScalarTypesRowUnsaved();
          var inserted = repo.insert(row, c);

          assertNotNull(inserted);
          assertNotNull(inserted.id());

          // Verify nullable fields are empty
          assertTrue(inserted.colTinyint().isEmpty());
          assertTrue(inserted.colSmallint().isEmpty());
          assertTrue(inserted.colInt().isEmpty());
          assertTrue(inserted.colBit().isEmpty());
          assertTrue(inserted.colVarchar().isEmpty());
          assertTrue(inserted.colDate().isEmpty());
          assertTrue(inserted.colUniqueidentifier().isEmpty());

          // Default value should be applied
          assertEquals("default_value", inserted.colNotNull());

          // ROWVERSION should still be generated
          assertNotNull(inserted.colRowversion());
        });
  }

  // ==================== Individual Type Tests ====================

  @Test
  public void testIntegerTypes() {
    SqlServerTestHelper.run(
        c -> {
          var row = createSampleRow();
          var inserted = repo.insert(row, c);

          assertEquals(Optional.of(Uint1.of(255)), inserted.colTinyint());
          assertEquals(Optional.of((short) 1000), inserted.colSmallint());
          assertEquals(Optional.of(100000), inserted.colInt());
          assertEquals(Optional.of(10000000000L), inserted.colBigint());
        });
  }

  @Test
  public void testMoneyTypes() {
    SqlServerTestHelper.run(
        c -> {
          var row = createSampleRow();
          var inserted = repo.insert(row, c);

          assertEquals(Optional.of(new BigDecimal("922337203685477.5807")), inserted.colMoney());
          assertEquals(Optional.of(new BigDecimal("214748.3647")), inserted.colSmallmoney());
        });
  }

  @Test
  public void testDateTimeTypes() {
    SqlServerTestHelper.run(
        c -> {
          var row = createSampleRow();
          var inserted = repo.insert(row, c);

          assertEquals(Optional.of(LocalDate.of(2025, 1, 15)), inserted.colDate());
          assertNotNull(inserted.colTime());
          assertNotNull(inserted.colDatetime());
          assertNotNull(inserted.colDatetime2());
          assertNotNull(inserted.colDatetimeoffset());
        });
  }

  @Test
  public void testUuidType() {
    SqlServerTestHelper.run(
        c -> {
          var uuid = UUID.randomUUID();
          var row = createSampleRow().withColUniqueidentifier(Optional.of(uuid));
          var inserted = repo.insert(row, c);

          assertEquals(Optional.of(uuid), inserted.colUniqueidentifier());
        });
  }

  @Test
  public void testXmlType() {
    SqlServerTestHelper.run(
        c -> {
          var xml = new Xml("<person><name>John</name><age>30</age></person>");
          var row = createSampleRow().withColXml(Optional.of(xml));
          var inserted = repo.insert(row, c);

          assertTrue(inserted.colXml().isPresent());
          assertTrue(inserted.colXml().get().value().contains("John"));
        });
  }

  @Test
  public void testJsonType() {
    SqlServerTestHelper.run(
        c -> {
          var json = new Json("{\"name\": \"test\", \"values\": [1, 2, 3]}");
          var row = createSampleRow().withColJson(Optional.of(json));
          var inserted = repo.insert(row, c);

          assertTrue(inserted.colJson().isPresent());
          assertTrue(inserted.colJson().get().value().contains("name"));
        });
  }

  @Test
  public void testBinaryTypes() {
    SqlServerTestHelper.run(
        c -> {
          var binaryData = new byte[] {0x00, 0x01, 0x02, (byte) 0xFF, (byte) 0xFE};
          var row = createSampleRow().withColVarbinary(Optional.of(binaryData));
          var inserted = repo.insert(row, c);

          assertTrue(inserted.colVarbinary().isPresent());
          assertArrayEquals(binaryData, inserted.colVarbinary().get());
        });
  }

  @Test
  public void testUnicodeStrings() {
    SqlServerTestHelper.run(
        c -> {
          var unicode = "Hello 世界 🌍 مرحبا";
          var row = createSampleRow().withColNvarchar(Optional.of(unicode));
          var inserted = repo.insert(row, c);

          assertEquals(Optional.of(unicode), inserted.colNvarchar());
        });
  }

  @Test
  public void testRowversionAutoGenerated() {
    SqlServerTestHelper.run(
        c -> {
          var row1 = repo.insert(createSampleRow(), c);
          var row2 = repo.insert(createSampleRow(), c);

          // Each row should have a unique rowversion
          assertNotNull(row1.colRowversion());
          assertNotNull(row2.colRowversion());
          assertFalse(java.util.Arrays.equals(row1.colRowversion(), row2.colRowversion()));
        });
  }
}
