package testdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.function.Consumer;
import java.util.function.Function;

public class Db2TestHelper {
  private static final String JDBC_URL = "jdbc:db2://localhost:50000/typr";
  private static final String USER = "db2inst1";
  private static final String PASSWORD = "password";

  public static <T> T apply(Function<Connection, T> f) {
    try {
      Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
      conn.setAutoCommit(false);
      try {
        return f.apply(conn);
      } finally {
        conn.rollback();
        conn.close();
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static void run(Consumer<Connection> f) {
    try {
      Connection conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
      conn.setAutoCommit(false);
      try {
        f.accept(conn);
      } finally {
        conn.rollback();
        conn.close();
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
