package testdb;

import dev.typr.foundations.connect.db2.Db2Config;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;

public class WithConnection {
  private static final Db2Config CONFIG =
      Db2Config.builder("localhost", 50000, "typr", "db2inst1", "password").build();

  public static <T> T apply(Function<Connection, T> f) {
    try (Connection conn = CONFIG.connect()) {
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
    try (Connection conn = CONFIG.connect()) {
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
