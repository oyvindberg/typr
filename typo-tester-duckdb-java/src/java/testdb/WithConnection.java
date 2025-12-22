package testdb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;

public class WithConnection {
  private static final String JDBC_URL = "jdbc:duckdb:db/duckdb/test.db";
  private static final String SCHEMA_PATH = "db/duckdb/00-schema.sql";
  private static boolean schemaInitialized = false;

  private static synchronized void ensureSchemaLoaded() {
    if (schemaInitialized) {
      return;
    }

    try (Connection conn = DriverManager.getConnection(JDBC_URL)) {
      // Check if schema is already loaded by trying to query a known table
      try {
        var stmt = conn.createStatement();
        stmt.executeQuery("SELECT 1 FROM customers LIMIT 1");
        stmt.close();
        schemaInitialized = true;
        return; // Schema already exists
      } catch (SQLException e) {
        // Table doesn't exist, need to load schema
      }

      // Load and execute schema
      String schemaSql = Files.readString(Path.of(SCHEMA_PATH));
      var stmt = conn.createStatement();
      stmt.execute(schemaSql);
      stmt.close();
      schemaInitialized = true;
    } catch (SQLException | IOException e) {
      throw new RuntimeException("Failed to initialize database schema", e);
    }
  }

  public static <T> T apply(Function<Connection, T> f) {
    ensureSchemaLoaded();
    try (Connection conn = DriverManager.getConnection(JDBC_URL)) {
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

  public static void run(Consumer<Connection> f) {
    ensureSchemaLoaded();
    try (Connection conn = DriverManager.getConnection(JDBC_URL)) {
      conn.setAutoCommit(false);
      try {
        f.accept(conn);
      } finally {
        conn.rollback();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
