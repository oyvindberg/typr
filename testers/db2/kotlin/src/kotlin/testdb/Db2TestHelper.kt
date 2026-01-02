package testdb

import java.sql.Connection
import java.sql.DriverManager

object Db2TestHelper {
    private const val JDBC_URL = "jdbc:db2://localhost:50000/typr"
    private const val USER = "db2inst1"
    private const val PASSWORD = "password"

    fun <T> apply(f: (Connection) -> T): T {
        val conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD)
        conn.autoCommit = false
        return try {
            f(conn)
        } finally {
            conn.rollback()
            conn.close()
        }
    }

    fun run(f: (Connection) -> Unit) {
        val conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD)
        conn.autoCommit = false
        try {
            f(conn)
        } finally {
            conn.rollback()
            conn.close()
        }
    }
}
