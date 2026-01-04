package testdb;

import static org.junit.Assert.*;

import dev.typr.foundations.data.Json;
import dev.typr.foundations.data.Uint1;
import dev.typr.foundations.data.Uint2;
import dev.typr.foundations.data.Uint4;
import dev.typr.foundations.data.Uint8;
import dev.typr.foundations.data.maria.Inet4;
import dev.typr.foundations.data.maria.Inet6;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import testdb.mariatest.*;
import testdb.mariatestnull.*;

/**
 * Comprehensive tests for all MariaDB data types. Tests the mariatest (all NOT NULL columns) and
 * mariatestnull (all nullable columns) tables.
 */
public class AllTypesTest {
  private final MariatestRepoImpl mariatestRepo = new MariatestRepoImpl();
  private final MariatestnullRepoImpl mariatestnullRepo = new MariatestnullRepoImpl();

  /** Create a sample row with all types populated */
  static MariatestRow createSampleRow(int id) {
    return new MariatestRow(
        (byte) 127, // tinyint
        (short) 32767, // smallint
        8388607, // mediumint
        new MariatestId(id), // int (PK)
        9223372036854775807L, // bigint
        Uint1.of(255), // tinyint unsigned
        Uint2.of(65535), // smallint unsigned
        Uint4.of(16777215L), // mediumint unsigned
        Uint4.of(4294967295L), // int unsigned
        Uint8.of(new BigInteger("18446744073709551615")), // bigint unsigned
        new BigDecimal("12345.67"), // decimal
        new BigDecimal("9876.5432"), // numeric
        3.14f, // float
        2.718281828, // double
        true, // bool
        new byte[] {(byte) 0xFF}, // bit(8)
        new byte[] {(byte) 0x01}, // bit(1)
        "char_val  ", // char(10)
        "varchar_value", // varchar
        "tinytext", // tinytext
        "text content", // text
        "mediumtext content", // mediumtext
        "longtext content", // longtext
        new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15}, // binary(16)
        new byte[] {1, 2, 3}, // varbinary
        new byte[] {4, 5, 6}, // tinyblob
        new byte[] {7, 8, 9}, // blob
        new byte[] {10, 11, 12}, // mediumblob
        new byte[] {13, 14, 15}, // longblob
        LocalDate.of(2025, 1, 15), // date
        LocalTime.of(14, 30, 45), // time
        LocalTime.of(14, 30, 45, 123456000), // time(6)
        LocalDateTime.of(2025, 1, 15, 14, 30, 45), // datetime
        LocalDateTime.of(2025, 1, 15, 14, 30, 45, 123456000), // datetime(6)
        LocalDateTime.now(), // timestamp (default)
        LocalDateTime.now(), // timestamp(6) (default)
        Year.of(2025), // year
        XYZSet.fromString("x,y"), // set
        new Json("{\"key\": \"value\"}"), // json
        new Inet4("192.168.1.1"), // inet4
        new Inet6("::1")); // inet6
  }

  @Test
  public void testInsertAndSelectAllTypes() {
    MariaDbTestHelper.run(
        c -> {
          var row = createSampleRow(1);
          var inserted = mariatestRepo.insert(row, c);

          assertNotNull(inserted);
          assertEquals(row.intCol(), inserted.intCol());
          assertEquals(row.tinyintCol(), inserted.tinyintCol());
          assertEquals(row.smallintCol(), inserted.smallintCol());
          assertEquals(row.bigintCol(), inserted.bigintCol());
          assertEquals(row.decimalCol(), inserted.decimalCol());
          assertEquals(row.boolCol(), inserted.boolCol());
          assertEquals(row.varcharCol(), inserted.varcharCol());
          assertEquals(row.dateCol(), inserted.dateCol());
          assertEquals(row.yearCol(), inserted.yearCol());
          assertEquals(row.inet4Col(), inserted.inet4Col());
          assertEquals(row.inet6Col(), inserted.inet6Col());

          // Verify we can select it back
          var found = mariatestRepo.selectById(inserted.intCol(), c);
          assertTrue(found.isPresent());
          assertEquals(inserted.intCol(), found.get().intCol());
        });
  }

  @Test
  public void testUpdateAllTypes() {
    MariaDbTestHelper.run(
        c -> {
          var row = createSampleRow(2);
          var inserted = mariatestRepo.insert(row, c);

          // Update some fields
          var updated =
              inserted
                  .withVarcharCol("updated_varchar")
                  .withDecimalCol(new BigDecimal("999.99"))
                  .withBoolCol(false);

          boolean wasUpdated = mariatestRepo.update(updated, c);
          assertTrue(wasUpdated);

          var found = mariatestRepo.selectById(inserted.intCol(), c).orElseThrow();
          assertEquals("updated_varchar", found.varcharCol());
          assertEquals(new BigDecimal("999.99"), found.decimalCol());
          assertEquals(false, found.boolCol());
        });
  }

  @Test
  public void testDeleteAllTypes() {
    MariaDbTestHelper.run(
        c -> {
          var row = createSampleRow(3);
          var inserted = mariatestRepo.insert(row, c);

          boolean deleted = mariatestRepo.deleteById(inserted.intCol(), c);
          assertTrue(deleted);

          var found = mariatestRepo.selectById(inserted.intCol(), c);
          assertFalse(found.isPresent());
        });
  }

  @Test
  public void testSelectAll() {
    MariaDbTestHelper.run(
        c -> {
          var row1 = createSampleRow(4);
          var row2 = createSampleRow(5);

          mariatestRepo.insert(row1, c);
          mariatestRepo.insert(row2, c);

          List<MariatestRow> all = mariatestRepo.selectAll(c);
          assertTrue(all.size() >= 2);
        });
  }

  // ==================== Nullable Types Tests ====================

  @Test
  public void testInsertNullableWithAllNulls() {
    MariaDbTestHelper.run(
        c -> {
          // Use the short constructor that sets all fields to UseDefault
          var unsaved = new MariatestnullRowUnsaved();

          var inserted = mariatestnullRepo.insert(unsaved, c);
          assertNotNull(inserted);

          // Verify all fields are empty
          assertTrue(inserted.tinyintCol().isEmpty());
          assertTrue(inserted.smallintCol().isEmpty());
          assertTrue(inserted.intCol().isEmpty());
          assertTrue(inserted.bigintCol().isEmpty());
          assertTrue(inserted.decimalCol().isEmpty());
          assertTrue(inserted.boolCol().isEmpty());
          assertTrue(inserted.varcharCol().isEmpty());
          assertTrue(inserted.dateCol().isEmpty());
          assertTrue(inserted.inet4Col().isEmpty());
          assertTrue(inserted.inet6Col().isEmpty());
        });
  }

  @Test
  public void testInsertNullableWithValues() {
    MariaDbTestHelper.run(
        c -> {
          // Explicitly test ALL 42 nullable columns with real values - use Row type directly
          var row =
              new MariatestnullRow(
                  Optional.of((byte) 42),
                  Optional.of((short) 1000),
                  Optional.of(50000),
                  Optional.of(100000),
                  Optional.of(1234567890L),
                  Optional.of(Uint1.of(200)),
                  Optional.of(Uint2.of(40000)),
                  Optional.of(Uint4.of(8000000L)),
                  Optional.of(Uint4.of(3000000000L)),
                  Optional.of(Uint8.of(new BigInteger("12345678901234567890"))),
                  Optional.of(new BigDecimal("123.45")),
                  Optional.of(new BigDecimal("678.90")),
                  Optional.of(1.5f),
                  Optional.of(2.5),
                  Optional.of(true),
                  Optional.of(new byte[] {(byte) 0xAB}),
                  Optional.of(new byte[] {(byte) 0x01}),
                  Optional.of("char      "),
                  Optional.of("varchar"),
                  Optional.of("tinytext"),
                  Optional.of("text"),
                  Optional.of("mediumtext"),
                  Optional.of("longtext"),
                  Optional.of(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}),
                  Optional.of(new byte[] {1, 2, 3}),
                  Optional.of(new byte[] {4, 5, 6}),
                  Optional.of(new byte[] {7, 8, 9}),
                  Optional.of(new byte[] {10, 11, 12}),
                  Optional.of(new byte[] {13, 14, 15}),
                  Optional.of(LocalDate.of(2025, 6, 15)),
                  Optional.of(LocalTime.of(10, 30)),
                  Optional.of(LocalTime.of(10, 30, 45, 500000000)),
                  Optional.of(LocalDateTime.of(2025, 6, 15, 10, 30)),
                  Optional.of(LocalDateTime.of(2025, 6, 15, 10, 30, 45, 500000000)),
                  Optional.of(LocalDateTime.now()),
                  Optional.of(LocalDateTime.now()),
                  Optional.of(Year.of(2024)),
                  Optional.of(XYZSet.fromString("y,z")),
                  Optional.of(new Json("{\"test\": true}")),
                  Optional.of(new Inet4("10.0.0.1")),
                  Optional.of(new Inet6("fe80::1")));

          var inserted = mariatestnullRepo.insert(row, c);
          assertNotNull(inserted);

          // Verify some fields
          assertEquals(Optional.of((byte) 42), inserted.tinyintCol());
          assertEquals(Optional.of((short) 1000), inserted.smallintCol());
          assertEquals(Optional.of(new BigDecimal("123.45")), inserted.decimalCol());
          assertEquals(Optional.of(true), inserted.boolCol());
          assertEquals(Optional.of("varchar"), inserted.varcharCol());
          assertEquals(Optional.of(LocalDate.of(2025, 6, 15)), inserted.dateCol());
          assertEquals(Optional.of(Year.of(2024)), inserted.yearCol());
          assertEquals(Optional.of(new Inet4("10.0.0.1")), inserted.inet4Col());
        });
  }

  // ==================== Individual Type Tests ====================

  @Test
  public void testIntegerTypes() {
    MariaDbTestHelper.run(
        c -> {
          var row = createSampleRow(10);
          var inserted = mariatestRepo.insert(row, c);

          assertEquals(Byte.valueOf((byte) 127), inserted.tinyintCol());
          assertEquals(Short.valueOf((short) 32767), inserted.smallintCol());
          assertEquals(Integer.valueOf(8388607), inserted.mediumintCol());
          assertEquals(Long.valueOf(9223372036854775807L), inserted.bigintCol());
        });
  }

  @Test
  public void testUnsignedTypes() {
    MariaDbTestHelper.run(
        c -> {
          var row = createSampleRow(11);
          var inserted = mariatestRepo.insert(row, c);

          assertEquals(Uint1.of(255), inserted.tinyintUCol());
          assertEquals(Uint2.of(65535), inserted.smallintUCol());
          assertEquals(Uint4.of(16777215L), inserted.mediumintUCol());
          assertEquals(Uint4.of(4294967295L), inserted.intUCol());
          assertEquals(Uint8.of(new BigInteger("18446744073709551615")), inserted.bigintUCol());
        });
  }

  @Test
  public void testDecimalTypes() {
    MariaDbTestHelper.run(
        c -> {
          var row = createSampleRow(12);
          var inserted = mariatestRepo.insert(row, c);

          assertEquals(new BigDecimal("12345.67"), inserted.decimalCol());
          assertEquals(new BigDecimal("9876.5432"), inserted.numericCol());
        });
  }

  @Test
  public void testDateTimeTypes() {
    MariaDbTestHelper.run(
        c -> {
          var row = createSampleRow(13);
          var inserted = mariatestRepo.insert(row, c);

          assertEquals(LocalDate.of(2025, 1, 15), inserted.dateCol());
          assertEquals(LocalTime.of(14, 30, 45), inserted.timeCol());
          assertEquals(Year.of(2025), inserted.yearCol());
          assertNotNull(inserted.timestampCol());
        });
  }

  @Test
  public void testSetType() {
    MariaDbTestHelper.run(
        c -> {
          // Test various set combinations
          var row1 = createSampleRow(20).withSetCol(XYZSet.of(List.of(XYZSetMember.x)));
          var inserted1 = mariatestRepo.insert(row1, c);
          assertEquals(XYZSet.of(List.of(XYZSetMember.x)), inserted1.setCol());

          var row2 = createSampleRow(21).withSetCol(XYZSet.fromString("x,y,z"));
          var inserted2 = mariatestRepo.insert(row2, c);
          assertEquals(
              XYZSet.of(List.of(XYZSetMember.x, XYZSetMember.y, XYZSetMember.z)),
              inserted2.setCol());
        });
  }

  @Test
  public void testInetTypes() {
    MariaDbTestHelper.run(
        c -> {
          var row =
              createSampleRow(30)
                  .withInet4Col(new Inet4("192.168.0.1"))
                  .withInet6Col(new Inet6("2001:db8::1"));
          var inserted = mariatestRepo.insert(row, c);

          assertEquals(new Inet4("192.168.0.1"), inserted.inet4Col());
          assertEquals(new Inet6("2001:db8::1"), inserted.inet6Col());
        });
  }

  @Test
  public void testJsonType() {
    MariaDbTestHelper.run(
        c -> {
          var jsonValue = new Json("{\"name\": \"test\", \"values\": [1, 2, 3]}");
          var row = createSampleRow(40).withJsonCol(jsonValue);
          var inserted = mariatestRepo.insert(row, c);

          assertNotNull(inserted.jsonCol());
          assertTrue(inserted.jsonCol().value().contains("name"));
        });
  }
}
