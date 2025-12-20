package testdb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Consumer;

/**
 * Test helper for DuckDB tests. DuckDB is an embedded database, so each test runs with a fresh
 * in-memory database.
 */
public interface DuckDbTestHelper {

  /**
   * Run a test with a fresh DuckDB connection. The schema is loaded automatically and the
   * connection is rolled back after the test.
   */
  static void run(Consumer<Connection> test) {
    try (Connection conn = DriverManager.getConnection("jdbc:duckdb:")) {
      conn.setAutoCommit(false);

      // Load the schema
      loadSchema(conn);

      try {
        test.accept(conn);
      } finally {
        conn.rollback();
      }
    } catch (SQLException e) {
      throw new RuntimeException("DuckDB test failed", e);
    }
  }

  /** Run a test with a connection, returning a result. */
  static <T> T runWithResult(SqlFunction<Connection, T> test) {
    try (Connection conn = DriverManager.getConnection("jdbc:duckdb:")) {
      conn.setAutoCommit(false);

      // Load the schema
      loadSchema(conn);

      try {
        return test.apply(conn);
      } finally {
        conn.rollback();
      }
    } catch (SQLException e) {
      throw new RuntimeException("DuckDB test failed", e);
    }
  }

  /** Load the DuckDB schema from the schema file. */
  private static void loadSchema(Connection conn) throws SQLException {
    try {
      // Try to find schema file relative to project root
      Path schemaPath = Path.of("db/duckdb/00-schema.sql");
      if (!Files.exists(schemaPath)) {
        // Try parent directory (when running from subproject)
        schemaPath = Path.of("../db/duckdb/00-schema.sql");
      }
      if (!Files.exists(schemaPath)) {
        // Try two levels up
        schemaPath = Path.of("../../db/duckdb/00-schema.sql");
      }

      if (Files.exists(schemaPath)) {
        String schemaSql = Files.readString(schemaPath);
        try (Statement stmt = conn.createStatement()) {
          stmt.execute(schemaSql);
        }
      } else {
        throw new RuntimeException(
            "Could not find DuckDB schema file. Looked in: db/duckdb/00-schema.sql");
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to read DuckDB schema file", e);
    }
  }

  @FunctionalInterface
  interface SqlFunction<T, R> {
    R apply(T t) throws SQLException;
  }
}
