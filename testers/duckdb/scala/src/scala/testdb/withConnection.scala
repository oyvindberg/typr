package testdb

import java.nio.file.{Files, Path}

object withConnection {
  private lazy val schemaSQL: String = Files.readString(Path.of("sql-init/duckdb/00-schema.sql"))

  private def createConnection(): java.sql.Connection = {
    val conn = java.sql.DriverManager.getConnection("jdbc:duckdb:")
    conn.createStatement().execute(schemaSQL)
    conn
  }

  def apply[T](f: java.sql.Connection => T): T = {
    val conn = createConnection()
    conn.setAutoCommit(false)
    try {
      f(conn)
    } finally {
      conn.rollback()
      conn.close()
    }
  }
}
