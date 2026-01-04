package dev.typr.foundations.hikari;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.typr.foundations.connect.ConnectionSettings;
import dev.typr.foundations.connect.DatabaseConfig;

/**
 * Factory for creating PooledDataSource instances.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * // With connection settings
 * var ds = HikariDataSourceFactory.create(
 *     PostgresConfig.builder("localhost", 5432, "mydb", "user", "pass").build(),
 *     ConnectionSettings.builder()
 *         .transactionIsolation(TransactionIsolation.READ_UNCOMMITTED)
 *         .build(),
 *     PoolConfig.builder()
 *         .maximumPoolSize(20)
 *         .build());
 *
 * var tx = ds.transactor();
 * tx.execute(conn -> repo.selectAll(conn));
 * }</pre>
 */
public final class HikariDataSourceFactory {

  private HikariDataSourceFactory() {}

  /**
   * Create a PooledDataSource with connection settings and pool configuration.
   *
   * @param config database configuration (URL, credentials, driver properties)
   * @param settings connection settings (isolation, autoCommit, readOnly, etc.)
   * @param pool pool configuration (sizing, timeouts, etc.)
   * @return configured PooledDataSource
   */
  public static PooledDataSource create(
      DatabaseConfig config, ConnectionSettings settings, PoolConfig pool) {
    HikariConfig hikari = new HikariConfig();

    // Connection settings from DatabaseConfig
    hikari.setJdbcUrl(config.jdbcUrl());
    hikari.setUsername(config.username());
    hikari.setPassword(config.password());

    // Driver properties from DatabaseConfig
    config.driverProperties().forEach(hikari::addDataSourceProperty);

    // Pool sizing
    hikari.setMaximumPoolSize(pool.maximumPoolSize());
    hikari.setMinimumIdle(pool.minimumIdle());

    // Timeouts
    hikari.setConnectionTimeout(pool.connectionTimeout().toMillis());
    hikari.setValidationTimeout(pool.validationTimeout().toMillis());
    hikari.setIdleTimeout(pool.idleTimeout().toMillis());
    hikari.setMaxLifetime(pool.maxLifetime().toMillis());
    hikari.setKeepaliveTime(pool.keepaliveTime().toMillis());
    hikari.setLeakDetectionThreshold(pool.leakDetectionThreshold().toMillis());

    // Connection settings
    if (settings.transactionIsolation() != null) {
      hikari.setTransactionIsolation(settings.transactionIsolation().jdbcName());
    }
    if (settings.autoCommit() != null) {
      hikari.setAutoCommit(settings.autoCommit());
    }
    if (settings.readOnly() != null) {
      hikari.setReadOnly(settings.readOnly());
    }
    if (settings.catalog() != null) {
      hikari.setCatalog(settings.catalog());
    }
    if (settings.schema() != null) {
      hikari.setSchema(settings.schema());
    }
    if (settings.connectionInitSql() != null) {
      hikari.setConnectionInitSql(settings.connectionInitSql());
    }

    // Pool-specific: connection test query
    if (pool.connectionTestQuery() != null) {
      hikari.setConnectionTestQuery(pool.connectionTestQuery());
    }

    // Pool naming
    if (pool.poolName() != null) {
      hikari.setPoolName(pool.poolName());
    }

    // Advanced
    if (pool.registerMbeans() != null) {
      hikari.setRegisterMbeans(pool.registerMbeans());
    }
    if (pool.allowPoolSuspension() != null) {
      hikari.setAllowPoolSuspension(pool.allowPoolSuspension());
    }
    if (pool.isolateInternalQueries() != null) {
      hikari.setIsolateInternalQueries(pool.isolateInternalQueries());
    }

    // Extra properties
    pool.extraProperties().forEach(hikari::addDataSourceProperty);

    return new PooledDataSource(new HikariDataSource(hikari));
  }

  /**
   * Create a PooledDataSource with connection settings and default pool configuration.
   *
   * @param config database configuration
   * @param settings connection settings
   * @return configured PooledDataSource
   */
  public static PooledDataSource create(DatabaseConfig config, ConnectionSettings settings) {
    return create(config, settings, PoolConfig.defaults());
  }

  /**
   * Create a PooledDataSource with default settings.
   *
   * @param config database configuration
   * @return configured PooledDataSource with driver defaults
   */
  public static PooledDataSource create(DatabaseConfig config) {
    return create(config, ConnectionSettings.EMPTY, PoolConfig.defaults());
  }

  /**
   * Create a PooledDataSource with pool configuration but default connection settings.
   *
   * @param config database configuration
   * @param pool pool configuration
   * @return configured PooledDataSource
   */
  public static PooledDataSource create(DatabaseConfig config, PoolConfig pool) {
    return create(config, ConnectionSettings.EMPTY, pool);
  }
}
