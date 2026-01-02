package testdb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;

public class DuckDbTestHelper {
  private static final String JDBC_URL = "jdbc:duckdb:";
  private static String schemaSQL = null;

  private static synchronized String getSchemaSQL() {
    if (schemaSQL == null) {
      try {
        Path schemaPath = Path.of("sql-init/duckdb/00-schema.sql");
        schemaSQL = Files.readString(schemaPath);
      } catch (IOException e) {
        throw new RuntimeException("Failed to read DuckDB schema", e);
      }
    }
    return schemaSQL;
  }

  private static Connection createConnection() throws SQLException {
    Connection conn = DriverManager.getConnection(JDBC_URL);
    conn.createStatement().execute(getSchemaSQL());
    return conn;
  }

  public static <T> T apply(Function<Connection, T> f) {
    try (Connection conn = createConnection()) {
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
    try (Connection conn = createConnection()) {
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
