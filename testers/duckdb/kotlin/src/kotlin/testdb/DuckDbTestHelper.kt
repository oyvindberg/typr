package testdb

import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager

object DuckDbTestHelper {
    private const val JDBC_URL = "jdbc:duckdb:"
    private val schemaSQL: String by lazy {
        Files.readString(Path.of("sql-init/duckdb/00-schema.sql"))
    }

    private fun createConnection(): Connection {
        val conn = DriverManager.getConnection(JDBC_URL)
        conn.createStatement().execute(schemaSQL)
        return conn
    }

    fun run(f: (Connection) -> Unit) {
        createConnection().use { conn ->
            conn.autoCommit = false
            try {
                f(conn)
            } finally {
                conn.rollback()
            }
        }
    }
}
