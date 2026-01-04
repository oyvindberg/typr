package dev.typr.foundations.connect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * A simple non-pooled connection source using DriverManager.
 *
 * <p>Suitable for scripts, tests, or low-volume use cases. For production use with connection
 * pooling, use {@code PooledDataSource} from the foundations-jdbc-hikari module.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * var ds = SimpleDataSource.create(
 *     PostgresConfig.builder("localhost", 5432, "mydb", "user", "pass").build(),
 *     ConnectionSettings.builder()
 *         .transactionIsolation(TransactionIsolation.READ_UNCOMMITTED)
 *         .build());
 *
 * var tx = ds.transactor(Transactor.testStrategy());
 * tx.execute(conn -> repo.selectAll(conn));
 * }</pre>
 */
public final class SimpleDataSource implements ConnectionSource {

  private final DatabaseConfig config;
  private final ConnectionSettings settings;

  private SimpleDataSource(DatabaseConfig config, ConnectionSettings settings) {
    this.config = config;
    this.settings = settings;
  }

  /**
   * Create a SimpleDataSource with connection settings.
   *
   * @param config database configuration
   * @param settings connection settings to apply
   * @return a new SimpleDataSource
   */
  public static SimpleDataSource create(DatabaseConfig config, ConnectionSettings settings) {
    return new SimpleDataSource(config, settings);
  }

  /**
   * Create a SimpleDataSource with default settings.
   *
   * @param config database configuration
   * @return a new SimpleDataSource with driver defaults
   */
  public static SimpleDataSource create(DatabaseConfig config) {
    return new SimpleDataSource(config, ConnectionSettings.EMPTY);
  }

  @Override
  public Connection getConnection() throws SQLException {
    Properties props = new Properties();
    props.setProperty("user", config.username());
    props.setProperty("password", config.password());
    config.driverProperties().forEach(props::setProperty);

    Connection conn = DriverManager.getConnection(config.jdbcUrl(), props);
    applySettings(conn);
    return conn;
  }

  private void applySettings(Connection conn) throws SQLException {
    if (settings.transactionIsolation() != null) {
      conn.setTransactionIsolation(settings.transactionIsolation().jdbcLevel());
    }
    if (settings.autoCommit() != null) {
      conn.setAutoCommit(settings.autoCommit());
    }
    if (settings.readOnly() != null) {
      conn.setReadOnly(settings.readOnly());
    }
    if (settings.catalog() != null) {
      conn.setCatalog(settings.catalog());
    }
    if (settings.schema() != null) {
      conn.setSchema(settings.schema());
    }
    if (settings.connectionInitSql() != null) {
      try (Statement stmt = conn.createStatement()) {
        stmt.execute(settings.connectionInitSql());
      }
    }
  }

  /** Get the database configuration. */
  public DatabaseConfig config() {
    return config;
  }

  /** Get the connection settings. */
  public ConnectionSettings settings() {
    return settings;
  }
}
