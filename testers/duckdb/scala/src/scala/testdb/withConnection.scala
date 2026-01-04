package testdb

import dev.typr.foundations.{SqlFunction, Transactor}
import dev.typr.foundations.connect.duckdb.DuckDbConfig
import java.nio.file.{Files, Path}
import java.sql.DriverManager

object withConnection {
  private lazy val schemaSQL: String = Files.readString(Path.of("sql-init/duckdb/00-schema.sql"))
  private val config = DuckDbConfig.inMemory().build()

  private def createConnection(): java.sql.Connection = {
    val conn = DriverManager.getConnection(config.jdbcUrl())
    conn.createStatement().execute(schemaSQL)
    conn
  }

  private val transactor = new Transactor(() => createConnection(), Transactor.testStrategy())

  def apply[T](f: java.sql.Connection => T): T = {
    transactor.execute[T]((conn => f(conn)): SqlFunction[java.sql.Connection, T])
  }
}
