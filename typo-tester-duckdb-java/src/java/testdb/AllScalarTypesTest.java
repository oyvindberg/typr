package testdb;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.Optional;
import java.util.UUID;
import org.junit.Test;
import testdb.all_scalar_types.*;
import typo.data.Json;

/**
 * Comprehensive test for all scalar types supported by DuckDB. Tests every column type including: -
 * Integer types (signed): TINYINT, SMALLINT, INTEGER, BIGINT, HUGEINT - Integer types (unsigned):
 * UTINYINT, USMALLINT, UINTEGER, UBIGINT - Floating point types (FLOAT, DOUBLE, DECIMAL) - String
 * types (VARCHAR, TEXT) - Boolean - Date/Time types (DATE, TIME, TIMESTAMP, TIMESTAMPTZ, INTERVAL)
 * - Binary types (BLOB) - UUID - JSON - Enum type (Mood)
 */
public class AllScalarTypesTest {
  private final AllScalarTypesRepoImpl repo = new AllScalarTypesRepoImpl();

  @Test
  public void testInsertAndSelectWithAllTypes() {
    WithConnection.run(
        c -> {
          // Insert a row with all types populated
          var inserted =
              repo.insert(
                  new AllScalarTypesRow(
                      new AllScalarTypesId(1000),
                      Optional.of((byte) 127), // col_tinyint
                      Optional.of((short) 32000), // col_smallint
                      Optional.of(12345), // col_integer
                      Optional.of(9876543210L), // col_bigint
                      Optional.of(new BigInteger("123456789012345")), // col_hugeint
                      Optional.of((short) 200), // col_utinyint (stored as Short)
                      Optional.of(60000), // col_usmallint (stored as Integer)
                      Optional.of(3000000000L), // col_uinteger (stored as Long)
                      Optional.of(new BigInteger("10000000000000000000")), // col_ubigint
                      Optional.of(3.14f), // col_float
                      Optional.of(2.718281828), // col_double
                      Optional.of(new BigDecimal("999.99")), // col_decimal
                      Optional.of(true), // col_boolean
                      Optional.of("varchar_value"), // col_varchar
                      Optional.of("text_value_with_longer_content"), // col_text
                      Optional.of(new byte[] {0x01, 0x02, 0x03, 0x04}), // col_blob
                      Optional.of(LocalDate.of(2024, 12, 19)), // col_date
                      Optional.of(LocalTime.of(14, 30, 45)), // col_time
                      Optional.of(LocalDateTime.of(2024, 12, 19, 14, 30, 45)), // col_timestamp
                      Optional.of(
                          OffsetDateTime.of(
                              2024, 12, 19, 14, 30, 45, 0, ZoneOffset.UTC)), // col_timestamptz
                      Optional.of(Duration.ofHours(24)), // col_interval
                      Optional.of(
                          UUID.fromString("550e8400-e29b-41d4-a716-446655440000")), // col_uuid
                      Optional.of(new Json("{\"key\": \"value\", \"number\": 42}")), // col_json
                      Optional.of(Mood.happy), // col_mood (enum)
                      "required_value"), // col_not_null
                  c);

          assertNotNull(inserted.id());

          // Select the inserted row
          var selected = repo.selectById(inserted.id(), c);
          assertTrue(selected.isPresent());

          var row = selected.get();
          assertEquals((byte) 127, row.colTinyint().get().byteValue());
          assertEquals((short) 32000, row.colSmallint().get().shortValue());
          assertEquals(12345, row.colInteger().get().intValue());
          assertEquals(9876543210L, row.colBigint().get().longValue());
          assertEquals(new BigInteger("123456789012345"), row.colHugeint().get());
          assertEquals((short) 200, row.colUtinyint().get().shortValue());
          assertEquals(60000, row.colUsmallint().get().intValue());
          assertEquals(3000000000L, row.colUinteger().get().longValue());
          assertEquals(new BigInteger("10000000000000000000"), row.colUbigint().get());
          assertEquals(3.14f, row.colFloat().get(), 0.001f);
          assertEquals(2.718281828, row.colDouble().get(), 0.0001);
          assertEquals(new BigDecimal("999.99"), row.colDecimal().get());
          assertEquals("varchar_value", row.colVarchar().get());
          assertEquals("text_value_with_longer_content", row.colText().get());
          assertTrue(row.colBoolean().get());
          assertEquals(LocalDate.of(2024, 12, 19), row.colDate().get());
          assertEquals(LocalTime.of(14, 30, 45), row.colTime().get());
          assertEquals(LocalDateTime.of(2024, 12, 19, 14, 30, 45), row.colTimestamp().get());
          assertEquals(
              OffsetDateTime.of(2024, 12, 19, 14, 30, 45, 0, ZoneOffset.UTC),
              row.colTimestamptz().get());
          assertEquals(Duration.ofHours(24), row.colInterval().get());
          assertArrayEquals(new byte[] {0x01, 0x02, 0x03, 0x04}, row.colBlob().get());
          assertEquals(
              UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), row.colUuid().get());
          assertEquals("{\"key\": \"value\", \"number\": 42}", row.colJson().get().value());
          assertEquals(Mood.happy, row.colMood().get());
          assertEquals("required_value", row.colNotNull());
        });
  }

  @Test
  public void testInsertWithNullableFields() {
    WithConnection.run(
        c -> {
          // Insert with minimal required fields (only id and colNotNull)
          var inserted =
              repo.insert(
                  new AllScalarTypesRow(
                      new AllScalarTypesId(1001),
                      Optional.empty(), // col_tinyint
                      Optional.empty(), // col_smallint
                      Optional.empty(), // col_integer
                      Optional.empty(), // col_bigint
                      Optional.empty(), // col_hugeint
                      Optional.empty(), // col_utinyint
                      Optional.empty(), // col_usmallint
                      Optional.empty(), // col_uinteger
                      Optional.empty(), // col_ubigint
                      Optional.empty(), // col_float
                      Optional.empty(), // col_double
                      Optional.empty(), // col_decimal
                      Optional.empty(), // col_boolean
                      Optional.empty(), // col_varchar
                      Optional.empty(), // col_text
                      Optional.empty(), // col_blob
                      Optional.empty(), // col_date
                      Optional.empty(), // col_time
                      Optional.empty(), // col_timestamp
                      Optional.empty(), // col_timestamptz
                      Optional.empty(), // col_interval
                      Optional.empty(), // col_uuid
                      Optional.empty(), // col_json
                      Optional.empty(), // col_mood
                      "minimal"), // col_not_null
                  c);

          var selected = repo.selectById(inserted.id(), c);
          assertTrue(selected.isPresent());
          assertEquals(Optional.empty(), selected.get().colVarchar());
          assertEquals(Optional.empty(), selected.get().colInteger());
          assertEquals("minimal", selected.get().colNotNull());
        });
  }

  @Test
  public void testUpdateAllFields() {
    WithConnection.run(
        c -> {
          var initial =
              repo.insert(
                  new AllScalarTypesRow(
                      new AllScalarTypesId(1002),
                      Optional.of((byte) 10),
                      Optional.of((short) 20),
                      Optional.of(30),
                      Optional.of(40L),
                      Optional.of(new BigInteger("50")),
                      Optional.of((short) 60),
                      Optional.of(70),
                      Optional.of(80L),
                      Optional.of(new BigInteger("90")),
                      Optional.of(50.0f),
                      Optional.of(60.0),
                      Optional.of(new BigDecimal("70.0")),
                      Optional.of(false),
                      Optional.of("initial"),
                      Optional.of("initial_text"),
                      Optional.of(new byte[] {0x0A}),
                      Optional.of(LocalDate.of(2024, 1, 1)),
                      Optional.of(LocalTime.of(10, 0, 0)),
                      Optional.of(LocalDateTime.of(2024, 1, 1, 10, 0, 0)),
                      Optional.of(OffsetDateTime.of(2024, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC)),
                      Optional.of(Duration.ofHours(1)),
                      Optional.of(UUID.randomUUID()),
                      Optional.of(new Json("{}")),
                      Optional.of(Mood.sad),
                      "initial_required"),
                  c);

          // Update all mutable fields
          var updated =
              initial
                  .withColTinyint(Optional.of((byte) 99))
                  .withColSmallint(Optional.of((short) 999))
                  .withColInteger(Optional.of(9999))
                  .withColBigint(Optional.of(99999L))
                  .withColHugeint(Optional.of(new BigInteger("999999")))
                  .withColUtinyint(Optional.of((short) 250))
                  .withColUsmallint(Optional.of(65000))
                  .withColUinteger(Optional.of(4000000000L))
                  .withColUbigint(Optional.of(new BigInteger("99999999999")))
                  .withColFloat(Optional.of(99.9f))
                  .withColDouble(Optional.of(999.9))
                  .withColDecimal(Optional.of(new BigDecimal("9999.99")))
                  .withColVarchar(Optional.of("updated"))
                  .withColText(Optional.of("updated_text"))
                  .withColBoolean(Optional.of(true))
                  .withColDate(Optional.of(LocalDate.of(2024, 12, 31)))
                  .withColTime(Optional.of(LocalTime.of(23, 59, 59)))
                  .withColTimestamp(Optional.of(LocalDateTime.of(2024, 12, 31, 23, 59, 59)))
                  .withColTimestamptz(
                      Optional.of(OffsetDateTime.of(2024, 12, 31, 23, 59, 59, 0, ZoneOffset.UTC)))
                  .withColInterval(Optional.of(Duration.ofDays(7)))
                  .withColBlob(Optional.of(new byte[] {(byte) 0xFF}))
                  .withColUuid(Optional.of(UUID.fromString("123e4567-e89b-12d3-a456-426614174000")))
                  .withColJson(Optional.of(new Json("{\"updated\": true}")))
                  .withColMood(Optional.of(Mood.happy))
                  .withColNotNull("updated_required");

          assertTrue(repo.update(updated, c));

          var selected = repo.selectById(initial.id(), c);
          assertTrue(selected.isPresent());
          var row = selected.get();

          assertEquals((byte) 99, row.colTinyint().get().byteValue());
          assertEquals((short) 999, row.colSmallint().get().shortValue());
          assertEquals(9999, row.colInteger().get().intValue());
          assertEquals(99999L, row.colBigint().get().longValue());
          assertEquals(new BigInteger("999999"), row.colHugeint().get());
          assertEquals(99.9f, row.colFloat().get(), 0.001f);
          assertEquals(999.9, row.colDouble().get(), 0.001);
          assertEquals(new BigDecimal("9999.99"), row.colDecimal().get());
          assertEquals("updated", row.colVarchar().get());
          assertEquals("updated_text", row.colText().get());
          assertTrue(row.colBoolean().get());
          assertEquals(LocalDate.of(2024, 12, 31), row.colDate().get());
          assertEquals(LocalTime.of(23, 59, 59), row.colTime().get());
          assertEquals(LocalDateTime.of(2024, 12, 31, 23, 59, 59), row.colTimestamp().get());
          assertArrayEquals(new byte[] {(byte) 0xFF}, row.colBlob().get());
          assertEquals(
              UUID.fromString("123e4567-e89b-12d3-a456-426614174000"), row.colUuid().get());
          assertEquals("{\"updated\": true}", row.colJson().get().value());
          assertEquals(Mood.happy, row.colMood().get());
          assertEquals("updated_required", row.colNotNull());
        });
  }

  @Test
  public void testSelectAll() {
    WithConnection.run(
        c -> {
          // Insert 3 rows
          for (int i = 0; i < 3; i++) {
            repo.insert(
                new AllScalarTypesRow(
                    new AllScalarTypesId(2000 + i),
                    Optional.of((byte) i),
                    Optional.of((short) i),
                    Optional.of(i),
                    Optional.of((long) i),
                    Optional.of(BigInteger.valueOf(i)),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.of((float) i),
                    Optional.of((double) i),
                    Optional.of(BigDecimal.valueOf(i)),
                    Optional.of(i % 2 == 0),
                    Optional.of("varchar" + i),
                    Optional.of("text" + i),
                    Optional.of(new byte[] {(byte) i}),
                    Optional.of(LocalDate.now().plusDays(i)),
                    Optional.of(LocalTime.now()),
                    Optional.of(LocalDateTime.now()),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.of(UUID.randomUUID()),
                    Optional.of(new Json("{}")),
                    Optional.of(Mood.neutral),
                    "required" + i),
                c);
          }

          var all = repo.selectAll(c);
          assertTrue(all.size() >= 3);
        });
  }

  @Test
  public void testDeleteById() {
    WithConnection.run(
        c -> {
          var inserted =
              repo.insert(
                  new AllScalarTypesRow(
                      new AllScalarTypesId(3000),
                      Optional.of((byte) 1),
                      Optional.of((short) 1),
                      Optional.of(1),
                      Optional.of(1L),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.of(1.0f),
                      Optional.of(1.0),
                      Optional.of(BigDecimal.ONE),
                      Optional.of(true),
                      Optional.of("delete"),
                      Optional.of("me"),
                      Optional.of(new byte[] {0x01}),
                      Optional.of(LocalDate.now()),
                      Optional.of(LocalTime.now()),
                      Optional.of(LocalDateTime.now()),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.of(UUID.randomUUID()),
                      Optional.of(new Json("{}")),
                      Optional.of(Mood.neutral),
                      "to_delete"),
                  c);

          assertTrue(repo.deleteById(inserted.id(), c));
          assertFalse(repo.selectById(inserted.id(), c).isPresent());
        });
  }

  @Test
  public void testSelectByIds() {
    WithConnection.run(
        c -> {
          var r1 =
              repo.insert(
                  new AllScalarTypesRow(
                      new AllScalarTypesId(4000),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.of("row1"),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.of(Mood.happy),
                      "req1"),
                  c);
          var r2 =
              repo.insert(
                  new AllScalarTypesRow(
                      new AllScalarTypesId(4001),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.of("row2"),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.of(Mood.sad),
                      "req2"),
                  c);
          var r3 =
              repo.insert(
                  new AllScalarTypesRow(
                      new AllScalarTypesId(4002),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.of("row3"),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.of(Mood.neutral),
                      "req3"),
                  c);

          var selected = repo.selectByIds(new AllScalarTypesId[] {r1.id(), r3.id()}, c);
          assertEquals(2, selected.size());
          assertTrue(selected.stream().anyMatch(row -> row.colVarchar().get().equals("row1")));
          assertTrue(selected.stream().anyMatch(row -> row.colVarchar().get().equals("row3")));
        });
  }

  @Test
  public void testDSLSelectWithWhere() {
    WithConnection.run(
        c -> {
          repo.insert(
              new AllScalarTypesRow(
                  new AllScalarTypesId(5000),
                  Optional.empty(),
                  Optional.of((short) 100),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.of(true),
                  Optional.of("alice"),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.of(Mood.happy),
                  "req1"),
              c);
          repo.insert(
              new AllScalarTypesRow(
                  new AllScalarTypesId(5001),
                  Optional.empty(),
                  Optional.of((short) 200),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.of(false),
                  Optional.of("bob"),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.of(Mood.sad),
                  "req2"),
              c);
          repo.insert(
              new AllScalarTypesRow(
                  new AllScalarTypesId(5002),
                  Optional.empty(),
                  Optional.of((short) 100),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.of(true),
                  Optional.of("charlie"),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.of(Mood.neutral),
                  "req3"),
              c);

          // Test WHERE with integer condition
          var shortIs100 = repo.select().where(f -> f.colSmallint().isEqual((short) 100)).toList(c);
          assertTrue(shortIs100.size() >= 2);

          // Test WHERE with string condition
          var nameIsBob = repo.select().where(f -> f.colVarchar().isEqual("bob")).toList(c);
          assertTrue(nameIsBob.size() >= 1);
          assertEquals("bob", nameIsBob.get(0).colVarchar().get());

          // Test WHERE with boolean condition
          var booleanIsTrue = repo.select().where(f -> f.colBoolean().isEqual(true)).toList(c);
          assertTrue(booleanIsTrue.size() >= 2);

          // Test WHERE with enum condition
          var happyMood = repo.select().where(f -> f.colMood().isEqual(Mood.happy)).toList(c);
          assertTrue(happyMood.size() >= 1);

          // Test multiple WHERE conditions
          var filtered =
              repo.select()
                  .where(f -> f.colSmallint().isEqual((short) 100))
                  .where(f -> f.colBoolean().isEqual(true))
                  .toList(c);
          assertTrue(filtered.size() >= 1);
          assertTrue(
              filtered.stream()
                  .anyMatch(
                      r ->
                          r.colVarchar().get().equals("alice")
                              || r.colVarchar().get().equals("charlie")));
        });
  }

  @Test
  public void testDSLUpdate() {
    WithConnection.run(
        c -> {
          var inserted =
              repo.insert(
                  new AllScalarTypesRow(
                      new AllScalarTypesId(6000),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.of(1),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.of(false),
                      Optional.of("before"),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.of(Mood.sad),
                      "before_req"),
                  c);

          repo.update()
              .setValue(f -> f.colVarchar(), "after")
              .setValue(f -> f.colInteger(), 999)
              .setValue(f -> f.colBoolean(), true)
              .setValue(f -> f.colMood(), Mood.happy)
              .where(f -> f.id().isEqual(inserted.id()))
              .execute(c);

          var updated = repo.selectById(inserted.id(), c);
          assertTrue(updated.isPresent());
          assertEquals("after", updated.get().colVarchar().get());
          assertEquals(999, updated.get().colInteger().get().intValue());
          assertTrue(updated.get().colBoolean().get());
          assertEquals(Mood.happy, updated.get().colMood().get());
        });
  }

  @Test
  public void testDSLDelete() {
    WithConnection.run(
        c -> {
          repo.insert(
              new AllScalarTypesRow(
                  new AllScalarTypesId(7000),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.of("delete_me"),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.of(Mood.neutral),
                  "del1"),
              c);
          repo.insert(
              new AllScalarTypesRow(
                  new AllScalarTypesId(7001),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.of("keep_me"),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.empty(),
                  Optional.of(Mood.neutral),
                  "del2"),
              c);

          repo.delete().where(f -> f.colVarchar().isEqual("delete_me")).execute(c);

          var remaining = repo.selectAll(c);
          assertTrue(
              remaining.stream().noneMatch(r -> r.colVarchar().equals(Optional.of("delete_me"))));
          assertTrue(
              remaining.stream().anyMatch(r -> r.colVarchar().equals(Optional.of("keep_me"))));
        });
  }

  @Test
  public void testEnumTypes() {
    WithConnection.run(
        c -> {
          // Test all Mood enum values
          var happy =
              repo.insert(
                  new AllScalarTypesRow(
                      new AllScalarTypesId(8000),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.of(Mood.happy),
                      "mood1"),
                  c);
          var sad =
              repo.insert(
                  new AllScalarTypesRow(
                      new AllScalarTypesId(8001),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.of(Mood.sad),
                      "mood2"),
                  c);
          var neutral =
              repo.insert(
                  new AllScalarTypesRow(
                      new AllScalarTypesId(8002),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.empty(),
                      Optional.of(Mood.neutral),
                      "mood3"),
                  c);

          assertEquals(Mood.happy, repo.selectById(happy.id(), c).get().colMood().get());
          assertEquals(Mood.sad, repo.selectById(sad.id(), c).get().colMood().get());
          assertEquals(Mood.neutral, repo.selectById(neutral.id(), c).get().colMood().get());
        });
  }
}
