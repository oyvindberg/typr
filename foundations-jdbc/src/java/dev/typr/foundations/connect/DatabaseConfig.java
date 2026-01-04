package dev.typr.foundations.connect;

import dev.typr.foundations.Transactor;
import java.util.Map;

/**
 * Configuration for connecting to a database. Implemented by database-specific config classes
 * (PostgresConfig, MariaDbConfig, SqlServerConfig, etc.).
 *
 * <p>Each implementation provides typed builder methods for all documented JDBC driver properties.
 *
 * <p>Example:
 *
 * <pre>{@code
 * var config = PostgresConfig.builder("localhost", 5432, "mydb", "user", "pass")
 *     .sslmode(PgSslMode.REQUIRE)
 *     .build();
 *
 * // Quick shortcut for scripts/tests
 * var tx = config.transactor(Transactor.testStrategy());
 *
 * // Or with connection settings
 * var tx = config.transactor(
 *     ConnectionSettings.builder()
 *         .transactionIsolation(TransactionIsolation.READ_UNCOMMITTED)
 *         .build(),
 *     Transactor.testStrategy());
 * }</pre>
 */
public interface DatabaseConfig {

  /** Get the JDBC URL for this database configuration. */
  String jdbcUrl();

  /** Get the username for authentication. */
  String username();

  /** Get the password for authentication. */
  String password();

  /** Get the database kind (POSTGRESQL, MARIADB, etc.). */
  DatabaseKind kind();

  /**
   * Get all driver-specific properties (excluding user/password which are handled separately).
   * These are passed to the JDBC driver via DataSource properties or connection URL parameters.
   */
  Map<String, String> driverProperties();

  /**
   * Create a non-pooled Transactor with the default strategy.
   *
   * <p>Shortcut for {@code SimpleDataSource.create(this).transactor()}.
   *
   * @return a Transactor using non-pooled connections
   */
  default Transactor transactor() {
    return SimpleDataSource.create(this).transactor();
  }

  /**
   * Create a non-pooled Transactor with a custom strategy.
   *
   * <p>Shortcut for {@code SimpleDataSource.create(this).transactor(strategy)}.
   *
   * @param strategy the transaction strategy
   * @return a Transactor using non-pooled connections
   */
  default Transactor transactor(Transactor.Strategy strategy) {
    return SimpleDataSource.create(this).transactor(strategy);
  }

  /**
   * Create a non-pooled Transactor with connection settings and the default strategy.
   *
   * <p>Shortcut for {@code SimpleDataSource.create(this, settings).transactor()}.
   *
   * @param settings connection settings
   * @return a Transactor using non-pooled connections
   */
  default Transactor transactor(ConnectionSettings settings) {
    return SimpleDataSource.create(this, settings).transactor();
  }

  /**
   * Create a non-pooled Transactor with connection settings and a custom strategy.
   *
   * <p>Shortcut for {@code SimpleDataSource.create(this, settings).transactor(strategy)}.
   *
   * @param settings connection settings
   * @param strategy the transaction strategy
   * @return a Transactor using non-pooled connections
   */
  default Transactor transactor(ConnectionSettings settings, Transactor.Strategy strategy) {
    return SimpleDataSource.create(this, settings).transactor(strategy);
  }
}
