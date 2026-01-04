package dev.typr.foundations.hikari;

import com.zaxxer.hikari.HikariDataSource;
import dev.typr.foundations.Transactor;
import dev.typr.foundations.Transactor.Strategy;
import dev.typr.foundations.connect.ConnectionSource;
import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 * A pooled connection source using HikariCP.
 *
 * <p>This class wraps a HikariDataSource and implements {@link ConnectionSource} for unified API
 * with {@link dev.typr.foundations.connect.SimpleDataSource}.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * var ds = PooledDataSource.create(
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
public final class PooledDataSource implements ConnectionSource, Closeable {

  private final HikariDataSource dataSource;

  PooledDataSource(HikariDataSource dataSource) {
    this.dataSource = dataSource;
  }

  /**
   * Get the underlying HikariDataSource.
   *
   * @return the wrapped HikariDataSource
   */
  public HikariDataSource unwrap() {
    return dataSource;
  }

  /**
   * Get this as a standard JDBC DataSource.
   *
   * @return this as DataSource
   */
  public DataSource asDataSource() {
    return dataSource;
  }

  @Override
  public Connection getConnection() throws SQLException {
    return dataSource.getConnection();
  }

  @Override
  public Transactor transactor() {
    return ConnectionSource.super.transactor();
  }

  @Override
  public Transactor transactor(Strategy strategy) {
    return ConnectionSource.super.transactor(strategy);
  }

  /**
   * Close the underlying connection pool.
   *
   * <p>This will close all connections in the pool and release resources.
   */
  @Override
  public void close() {
    dataSource.close();
  }

  /**
   * Check if the pool is closed.
   *
   * @return true if the pool has been closed
   */
  public boolean isClosed() {
    return dataSource.isClosed();
  }

  /**
   * Check if the pool is running (not suspended or closed).
   *
   * @return true if the pool is running
   */
  public boolean isRunning() {
    return dataSource.isRunning();
  }
}
