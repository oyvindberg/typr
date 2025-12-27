package dev.typr.foundations;

import dev.typr.foundations.data.JsonValue;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.Test;

/** Tests for DB2 type codecs. Tests all types defined in Db2Types. */
public class Db2TypeTest {

  record TestPair<A>(A t0, Optional<A> t1) {}

  record Db2TypeAndExample<A>(
      Db2Type<A> type, A example, boolean hasIdentity, boolean jsonDbWorks) {
    public Db2TypeAndExample(Db2Type<A> type, A example) {
      this(type, example, true, true);
    }

    /** Skip JSON DB roundtrip test (DB2's JSON functions have limitations) */
    public Db2TypeAndExample<A> noJsonDb() {
      return new Db2TypeAndExample<>(type, example, hasIdentity, false);
    }

    public Db2TypeAndExample<A> noIdentity() {
      return new Db2TypeAndExample<>(type, example, false, jsonDbWorks);
    }
  }

  List<Db2TypeAndExample<?>> All =
      List.of(
          // ==================== Integer Types ====================
          new Db2TypeAndExample<>(Db2Types.smallint, (short) 4242),
          new Db2TypeAndExample<>(Db2Types.smallint, Short.MIN_VALUE), // Edge case: min value
          new Db2TypeAndExample<>(Db2Types.smallint, Short.MAX_VALUE), // Edge case: max value
          new Db2TypeAndExample<>(Db2Types.smallint, (short) 0), // Edge case: zero
          new Db2TypeAndExample<>(Db2Types.integer, 42424242),
          new Db2TypeAndExample<>(Db2Types.integer, Integer.MIN_VALUE), // Edge case: min value
          new Db2TypeAndExample<>(Db2Types.integer, Integer.MAX_VALUE), // Edge case: max value
          new Db2TypeAndExample<>(Db2Types.integer, 0), // Edge case: zero
          new Db2TypeAndExample<>(Db2Types.bigint, 4242424242424242L),
          new Db2TypeAndExample<>(Db2Types.bigint, Long.MIN_VALUE), // Edge case: min value
          new Db2TypeAndExample<>(Db2Types.bigint, Long.MAX_VALUE), // Edge case: max value
          new Db2TypeAndExample<>(Db2Types.bigint, 0L), // Edge case: zero

          // ==================== Fixed-Point Types ====================
          new Db2TypeAndExample<>(Db2Types.decimal(10, 2), new BigDecimal("12345.67")),
          new Db2TypeAndExample<>(
              Db2Types.decimal(10, 2), new BigDecimal("0.00")), // Edge case: zero (with scale)
          new Db2TypeAndExample<>(Db2Types.decimal(10, 2), new BigDecimal("-99999.99")), // Negative
          new Db2TypeAndExample<>(
              Db2Types.decimal(10, 2), new BigDecimal("12345678.90")), // With precision

          // DECFLOAT - DB2-specific decimal floating point
          new Db2TypeAndExample<>(Db2Types.decfloat, new BigDecimal("3.141592653589793")),
          new Db2TypeAndExample<>(Db2Types.decfloat(16), new BigDecimal("1.234567890123456E10")),

          // ==================== Floating-Point Types ====================
          new Db2TypeAndExample<>(Db2Types.real, 3.14159f),
          new Db2TypeAndExample<>(Db2Types.real, 0.0f), // Edge case: zero
          new Db2TypeAndExample<>(Db2Types.real, -Float.MAX_VALUE), // Edge case: min value
          new Db2TypeAndExample<>(Db2Types.real, Float.MAX_VALUE), // Edge case: max value
          new Db2TypeAndExample<>(Db2Types.real, -1.0f), // Negative
          new Db2TypeAndExample<>(Db2Types.double_, 3.141592653589793),
          new Db2TypeAndExample<>(Db2Types.double_, 0.0), // Edge case: zero
          new Db2TypeAndExample<>(Db2Types.double_, -Double.MAX_VALUE)
              .noJsonDb(), // DB2 JSON can't represent this value (SQLCODE=-16402)
          new Db2TypeAndExample<>(Db2Types.double_, Double.MAX_VALUE)
              .noJsonDb(), // DB2 JSON can't represent this value (SQLCODE=-16402)
          new Db2TypeAndExample<>(Db2Types.double_, -1.0), // Negative

          // ==================== Boolean Type ====================
          new Db2TypeAndExample<>(Db2Types.boolean_, true),
          new Db2TypeAndExample<>(Db2Types.boolean_, false),

          // ==================== String Types (SBCS) ====================
          new Db2TypeAndExample<>(Db2Types.char_(10), "Hello     ")
              .noJsonDb(), // DB2 JSON trims trailing spaces from CHAR
          new Db2TypeAndExample<>(Db2Types.char_(5), "12345"), // Exact length, no trailing spaces
          new Db2TypeAndExample<>(Db2Types.varchar(255), "Hello, DB2!"),
          new Db2TypeAndExample<>(Db2Types.varchar(255), ""), // Edge case: empty string
          new Db2TypeAndExample<>(Db2Types.varchar(100), "Variable length string"),
          new Db2TypeAndExample<>(
              Db2Types.varchar(255), "Special chars: äöü ñ 中文"), // Unicode in VARCHAR
          new Db2TypeAndExample<>(Db2Types.clob, "This is a CLOB value with more text."),

          // ==================== String Types (DBCS - Double-Byte) ====================
          // GRAPHIC/VARGRAPHIC not supported by DB2's JSON_OBJECT (SQLCODE=-171)
          new Db2TypeAndExample<>(Db2Types.graphic(5), "ＡＢＣＤＥ").noJsonDb(), // Full-width chars
          new Db2TypeAndExample<>(Db2Types.vargraphic(50), "日本語テスト").noJsonDb(), // Japanese
          new Db2TypeAndExample<>(Db2Types.vargraphic(50), "中文测试").noJsonDb(), // Chinese
          new Db2TypeAndExample<>(Db2Types.dbclob, "Double-byte CLOB content: 한글"),

          // ==================== Binary Types ====================
          new Db2TypeAndExample<>(Db2Types.binary(4), new byte[] {0x01, 0x02, 0x03, 0x04}),
          new Db2TypeAndExample<>(
              Db2Types.varbinary(100),
              new byte[] {(byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF}),
          new Db2TypeAndExample<>(Db2Types.varbinary(100), new byte[] {}), // Edge case: empty
          new Db2TypeAndExample<>(
              Db2Types.varbinary(100),
              new byte[] {0x00, 0x7F, (byte) 0x80, (byte) 0xFF}), // Boundary bytes
          new Db2TypeAndExample<>(
                  Db2Types.blob, new byte[] {0x42, 0x4C, 0x4F, 0x42}) // "BLOB" in hex
              .noIdentity(),

          // ==================== Date/Time Types ====================
          new Db2TypeAndExample<>(Db2Types.date, LocalDate.of(2024, 6, 15)),
          new Db2TypeAndExample<>(Db2Types.date, LocalDate.of(1970, 1, 1)), // Edge case: epoch
          new Db2TypeAndExample<>(Db2Types.date, LocalDate.of(2099, 12, 31)), // Future date
          new Db2TypeAndExample<>(Db2Types.time, LocalTime.of(14, 30, 45)),
          new Db2TypeAndExample<>(Db2Types.time, LocalTime.of(0, 0, 0)), // Edge case: midnight
          new Db2TypeAndExample<>(Db2Types.time, LocalTime.of(23, 59, 59)), // Edge case: end of day
          new Db2TypeAndExample<>(Db2Types.timestamp, LocalDateTime.of(2024, 6, 15, 14, 30, 45)),
          new Db2TypeAndExample<>(
              Db2Types.timestamp, LocalDateTime.of(1970, 1, 1, 0, 0, 0)), // Edge case: epoch
          new Db2TypeAndExample<>(
              Db2Types.timestamp(6),
              LocalDateTime.of(2024, 6, 15, 14, 30, 45, 123456000)), // Microseconds

          // ==================== Special Types ====================
          // DB2 normalizes XML: strips XML declaration, so use pre-normalized values
          // XML types can't be compared with = in SQL, so mark as noIdentity
          new Db2TypeAndExample<>(Db2Types.xml, "<root><element>value</element></root>")
              .noIdentity(),
          new Db2TypeAndExample<>(Db2Types.xml, "<simple/>").noIdentity());

  // Note: DB2 does not support arrays as a column type like PostgreSQL
  // Array operations in DB2 are handled via ARRAY data type in SQL PL only

  // Connection helper for DB2
  static <T> T withConnection(SqlFunction<Connection, T> f) {
    try (var conn =
        java.sql.DriverManager.getConnection(
            "jdbc:db2://localhost:50000/typr:user=db2inst1;password=password;")) {
      conn.setAutoCommit(false);
      try {
        return f.apply(conn);
      } finally {
        conn.rollback();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void test() {
    System.out.println("Testing DB2 type codecs...\n");

    int totalPassed = 0;
    int totalFailed = 0;
    int totalSkipped = 0;

    // Test JSON roundtrip first (no database connection needed)
    System.out.println("=== JSON Roundtrip Tests ===");
    for (Db2TypeAndExample<?> t : All) {
      try {
        boolean passed = testJsonRoundtrip(t);
        if (passed) {
          totalPassed++;
        } else {
          totalSkipped++;
        }
      } catch (Exception e) {
        System.out.println("  FAILED " + t.type.typename().sqlType() + ": " + e.getMessage());
        e.printStackTrace();
        totalFailed++;
      }
    }
    System.out.println();

    final int jsonPassed = totalPassed;
    final int jsonFailed = totalFailed;
    final int jsonSkipped = totalSkipped;

    withConnection(
        conn -> {
          int passed = jsonPassed;
          int failed = jsonFailed;
          int skipped = jsonSkipped;

          // Test native type roundtrip
          System.out.println("=== Native Type Roundtrip Tests ===");
          for (Db2TypeAndExample<?> t : All) {
            String typeName = t.type.typename().sqlType();
            try {
              System.out.println(
                  "Testing " + typeName + " with example '" + format(t.example) + "'");
              testCase(conn, t);
              System.out.println("  PASSED\n");
              passed++;
            } catch (Exception e) {
              System.out.println("  FAILED: " + e.getMessage() + "\n");
              e.printStackTrace();
              failed++;
            }
          }

          // Test JSON DB roundtrip
          System.out.println("\n=== JSON DB Roundtrip Tests ===");
          for (Db2TypeAndExample<?> t : All) {
            String typeName = t.type.typename().sqlType();
            if (!t.jsonDbWorks) {
              System.out.println("  SKIP JSON DB: " + typeName + " (DB2 JSON limitations)");
              skipped++;
              continue;
            }
            try {
              boolean testPassed = testJsonDbRoundtrip(conn, t);
              if (testPassed) {
                passed++;
              } else {
                // UnsupportedOperationException was caught inside method
                skipped++;
              }
            } catch (Exception e) {
              System.out.println("  FAILED " + typeName + ": " + e.getMessage() + "\n");
              e.printStackTrace();
              failed++;
            }
          }

          System.out.println("\n=====================================");
          int total = All.size() * 3; // JSON roundtrip + native + JSON DB
          System.out.println(
              "Results: "
                  + passed
                  + " passed, "
                  + failed
                  + " failed, "
                  + skipped
                  + " skipped out of "
                  + total
                  + " tests");
          System.out.println("=====================================");

          if (failed > 0) {
            throw new RuntimeException(failed + " tests failed");
          }

          return null;
        });
  }

  /**
   * Test JSON roundtrip in-memory. Returns true if test passed/skipped, false if type doesn't
   * support JSON.
   */
  static <A> boolean testJsonRoundtrip(Db2TypeAndExample<A> t) {
    try {
      Db2Json<A> jsonCodec = t.type.db2Json();
      A original = t.example;

      // Test toJson -> encode -> parse -> fromJson roundtrip (in-memory)
      JsonValue jsonValue = jsonCodec.toJson(original);
      String encoded = jsonValue.encode();
      JsonValue parsed = JsonValue.parse(encoded);
      A decoded = jsonCodec.fromJson(parsed);

      System.out.println(
          "JSON roundtrip "
              + t.type.typename().sqlType()
              + ": "
              + format(original)
              + " -> "
              + encoded
              + " -> "
              + format(decoded));

      if (t.hasIdentity && !areEqual(decoded, original)) {
        throw new RuntimeException(
            "JSON roundtrip failed for "
                + t.type.typename().sqlType()
                + ": expected '"
                + format(original)
                + "' but got '"
                + format(decoded)
                + "'");
      }
      return true;
    } catch (UnsupportedOperationException e) {
      // Type explicitly doesn't support JSON (e.g., GRAPHIC, VARBINARY)
      System.out.println(
          "JSON roundtrip " + t.type.typename().sqlType() + ": SKIP (" + e.getMessage() + ")");
      return false;
    } catch (Exception e) {
      throw new RuntimeException(
          "JSON roundtrip test failed for " + t.type.typename().sqlType(), e);
    }
  }

  /**
   * Test JSON roundtrip through the database. Returns true if test passed, false if type doesn't
   * support JSON.
   */
  static <A> boolean testJsonDbRoundtrip(Connection conn, Db2TypeAndExample<A> t)
      throws SQLException {
    Db2Json<A> jsonCodec = t.type.db2Json();
    A original = t.example;
    String sqlType = t.type.typename().sqlType();

    // Check if JSON is supported by attempting toJson - will throw UnsupportedOperationException
    // if not
    try {
      jsonCodec.toJson(original);
    } catch (UnsupportedOperationException e) {
      System.out.println("JSON DB roundtrip " + sqlType + ": SKIP (" + e.getMessage() + ")");
      return false;
    }

    // Use a regular table instead of GLOBAL TEMPORARY TABLE
    // since GLOBAL TEMPORARY TABLE requires a user temporary tablespace
    String tableName = "DB2INST1.TYPR_JSON_RT_TEST";
    try {
      conn.createStatement().execute("DROP TABLE " + tableName);
    } catch (SQLException e) {
      // Table might not exist, ignore
    }
    conn.createStatement().execute("CREATE TABLE " + tableName + " (v " + sqlType + ")");

    try {
      // Insert value using native type
      var insert = conn.prepareStatement("INSERT INTO " + tableName + " (v) VALUES (?)");
      t.type.write().set(insert, 1, original);
      insert.execute();
      insert.close();

      // Select back as JSON using JSON_OBJECT (DB2 syntax: KEY 'key' VALUE value)
      var select = conn.prepareStatement("SELECT JSON_OBJECT(KEY 'v' VALUE v) FROM " + tableName);
      select.execute();
      var rs = select.getResultSet();

      if (!rs.next()) {
        throw new RuntimeException("No rows returned");
      }

      // Read the JSON string back from the database
      String jsonFromDb = rs.getString(1);
      select.close();

      // Parse the JSON object and extract 'v' field
      JsonValue parsedFromDb = JsonValue.parse(jsonFromDb);
      JsonValue fieldValue = ((JsonValue.JObject) parsedFromDb).get("v");
      A decoded = jsonCodec.fromJson(fieldValue);

      System.out.println(
          "JSON DB roundtrip "
              + sqlType
              + ": "
              + format(original)
              + " -> DB -> "
              + jsonFromDb
              + " -> "
              + format(decoded));

      // Use tolerance comparison for JSON DB because DB2's JSON_OBJECT has limited precision
      // for binary floating point types (REAL: 6 digits, DOUBLE: 14 digits)
      if (t.hasIdentity && !areEqual(decoded, original, true)) {
        throw new RuntimeException(
            "JSON DB roundtrip failed for "
                + sqlType
                + ": expected '"
                + format(original)
                + "' but got '"
                + format(decoded)
                + "'");
      }
      return true;
    } finally {
      try {
        conn.createStatement().execute("DROP TABLE " + tableName);
      } catch (SQLException e) {
        // Ignore cleanup errors
      }
    }
  }

  static <A> void testCase(Connection conn, Db2TypeAndExample<A> t) throws SQLException {
    String sqlType = t.type.typename().sqlType();

    // Use a regular table instead of GLOBAL TEMPORARY TABLE
    // since GLOBAL TEMPORARY TABLE requires a user temporary tablespace
    String tableName = "DB2INST1.TYPR_TYPE_TEST";
    try {
      conn.createStatement().execute("DROP TABLE " + tableName);
    } catch (SQLException e) {
      // Table might not exist, ignore
    }
    conn.createStatement().execute("CREATE TABLE " + tableName + " (v " + sqlType + ")");

    try {
      // Insert using PreparedStatement
      var insert = conn.prepareStatement("INSERT INTO " + tableName + " (v) VALUES (?)");
      A expected = t.example;
      t.type.write().set(insert, 1, expected);
      insert.execute();
      insert.close();

      // Select and verify
      final PreparedStatement select;
      if (t.hasIdentity) {
        select =
            conn.prepareStatement(
                "SELECT v, CAST(NULL AS " + sqlType + ") FROM " + tableName + " WHERE v = ?");
        t.type.write().set(select, 1, expected);
      } else {
        select = conn.prepareStatement("SELECT v, CAST(NULL AS " + sqlType + ") FROM " + tableName);
      }

      select.execute();
      var rs = select.getResultSet();

      if (!rs.next()) {
        throw new RuntimeException("No rows returned");
      }

      // Read the value
      A actual = t.type.read().read(rs, 1);
      // Read the null value using opt()
      Optional<A> actualNull = t.type.opt().read().read(rs, 2);

      select.close();

      assertEquals(actual, expected, "value mismatch");
      assertEquals(actualNull, Optional.empty(), "null value mismatch");

    } finally {
      // Drop temp table
      try {
        conn.createStatement().execute("DROP TABLE " + tableName);
      } catch (SQLException e) {
        // Ignore cleanup errors
      }
    }
  }

  static <A> void assertEquals(A actual, A expected, String message) {
    if (!areEqual(actual, expected)) {
      throw new RuntimeException(
          message + ": actual='" + format(actual) + "' expected='" + format(expected) + "'");
    }
  }

  static <A> boolean areEqual(A actual, A expected) {
    return areEqual(actual, expected, false);
  }

  /**
   * Compare values for equality. When jsonDbTolerance is true, use relative tolerance for
   * Float/Double because DB2's JSON_OBJECT uses limited-precision scientific notation.
   */
  static <A> boolean areEqual(A actual, A expected, boolean jsonDbTolerance) {
    if (expected == null && actual == null) return true;
    if (expected == null || actual == null) return false;

    if (expected instanceof byte[]) {
      return Arrays.equals((byte[]) actual, (byte[]) expected);
    }
    if (expected instanceof Object[]) {
      return Arrays.deepEquals((Object[]) actual, (Object[]) expected);
    }

    // DB2 JSON_OBJECT uses limited precision for binary floating point:
    // - REAL: 6 significant digits (vs 7 for 32-bit float)
    // - DOUBLE: 14 significant digits (vs 15-16 for 64-bit float)
    if (jsonDbTolerance) {
      if (expected instanceof Float) {
        float exp = (Float) expected;
        float act = (Float) actual;
        if (exp == 0.0f) return act == 0.0f;
        // Allow ~2 digits of precision loss for REAL
        return Math.abs((act - exp) / exp) < 1e-5;
      }
      if (expected instanceof Double) {
        double exp = (Double) expected;
        double act = (Double) actual;
        if (exp == 0.0) return act == 0.0;
        // Allow ~2 digits of precision loss for DOUBLE
        return Math.abs((act - exp) / exp) < 1e-13;
      }
    }

    return actual.equals(expected);
  }

  static <A> String format(A a) {
    if (a == null) return "null";
    if (a instanceof byte[]) {
      return bytesToHex((byte[]) a);
    }
    if (a instanceof Object[]) {
      return Arrays.deepToString((Object[]) a);
    }
    return a.toString();
  }

  static String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (int i = 0; i < bytes.length; i++) {
      if (i > 0) sb.append(", ");
      sb.append(String.format("0x%02X", bytes[i]));
    }
    sb.append("]");
    return sb.toString();
  }
}
