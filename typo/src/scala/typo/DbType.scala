package typo

import java.sql.Connection
import typo.internal.codegen.{DbAdapter, DuckDbAdapter, MariaDbAdapter, PostgresAdapter}

sealed trait DbType {

  /** Get the database adapter for code generation */
  def adapter: DbAdapter
}

object DbType {
  case object PostgreSQL extends DbType {
    val adapter: DbAdapter = PostgresAdapter
  }
  case object MariaDB extends DbType {
    val adapter: DbAdapter = MariaDbAdapter
  }
  case object MySQL extends DbType {
    // MySQL uses MariaDB adapter for now (syntax is compatible)
    val adapter: DbAdapter = MariaDbAdapter
  }
  case object DuckDB extends DbType {
    val adapter: DbAdapter = DuckDbAdapter
  }

  def detect(connection: Connection): DbType = {
    val metadata = connection.getMetaData
    val productName = metadata.getDatabaseProductName.toLowerCase
    productName match {
      case name if name.contains("postgresql") => PostgreSQL
      case name if name.contains("mariadb")    => MariaDB
      case name if name.contains("mysql")      => MySQL
      case name if name.contains("duckdb")     => DuckDB
      case other                               => sys.error(s"Unsupported database: $other")
    }
  }

  def detectFromDriver(connection: Connection): DbType = {
    val driverName = connection.getMetaData.getDriverName.toLowerCase
    driverName match {
      case name if name.contains("postgresql") => PostgreSQL
      case name if name.contains("mariadb")    => MariaDB
      case name if name.contains("mysql")      => MySQL
      case name if name.contains("duckdb")     => DuckDB
      case other                               => sys.error(s"Unknown database driver: $other")
    }
  }
}
