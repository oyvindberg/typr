package dev.typr.foundations.connect;

import dev.typr.foundations.Transactor;
import dev.typr.foundations.Transactor.Strategy;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * A source of database connections with configured settings.
 *
 * <p>This interface abstracts over pooled and non-pooled connection sources, providing a unified
 * API for obtaining connections and transactors.
 *
 * <p>Implementations:
 *
 * <ul>
 *   <li>{@link SimpleDataSource} - Non-pooled connections via DriverManager
 *   <li>{@code PooledDataSource} - Pooled connections via HikariCP (in foundations-jdbc-hikari)
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * // Create a connection source (pooled or non-pooled)
 * var ds = SimpleDataSource.create(
 *     PostgresConfig.builder("localhost", 5432, "mydb", "user", "pass").build(),
 *     ConnectionSettings.builder()
 *         .transactionIsolation(TransactionIsolation.READ_UNCOMMITTED)
 *         .build());
 *
 * // Get a transactor
 * var tx = ds.transactor(Transactor.testStrategy());
 * tx.execute(conn -> repo.selectAll(conn));
 * }</pre>
 */
public interface ConnectionSource {

  /**
   * Get a connection from this source.
   *
   * @return a configured database connection
   * @throws SQLException if unable to get a connection
   */
  Connection getConnection() throws SQLException;

  /**
   * Create a Transactor with the default strategy (manual transactions with commit on success).
   *
   * @return a Transactor configured for manual transaction management
   */
  default Transactor transactor() {
    return transactor(Transactor.defaultStrategy());
  }

  /**
   * Create a Transactor with a custom strategy.
   *
   * @param strategy the transaction management strategy
   * @return a Transactor configured with the provided strategy
   */
  default Transactor transactor(Strategy strategy) {
    return new Transactor(this::getConnectionUnchecked, strategy);
  }

  private Connection getConnectionUnchecked() {
    try {
      return getConnection();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to get connection", e);
    }
  }
}
