package testdb

object withConnection {
  def apply[T](f: java.sql.Connection => T): T = {
    val conn = java.sql.DriverManager.getConnection(
      "jdbc:db2://localhost:50000/typr",
      "db2inst1",
      "password"
    )
    conn.setAutoCommit(false)
    try {
      f(conn)
    } finally {
      conn.rollback()
      conn.close()
    }
  }
}
