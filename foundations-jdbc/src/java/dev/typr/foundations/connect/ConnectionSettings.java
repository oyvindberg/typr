package dev.typr.foundations.connect;

/**
 * Settings applied to database connections. These settings are common whether using a connection
 * pool (HikariCP) or plain DriverManager connections.
 *
 * <p>Pass to {@link SimpleDataSource#create} or {@code PooledDataSource.create} to configure
 * connection behavior.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * var settings = ConnectionSettings.builder()
 *     .transactionIsolation(TransactionIsolation.READ_UNCOMMITTED)
 *     .readOnly(true)
 *     .build();
 *
 * var ds = SimpleDataSource.create(config, settings);
 * var tx = ds.transactor(Transactor.testStrategy());
 * }</pre>
 */
public record ConnectionSettings(
    TransactionIsolation transactionIsolation,
    Boolean autoCommit,
    Boolean readOnly,
    String catalog,
    String schema,
    String connectionInitSql) {

  /** Empty settings - use driver defaults for everything. */
  public static final ConnectionSettings EMPTY =
      new ConnectionSettings(null, null, null, null, null, null);

  /** Create a builder for ConnectionSettings. */
  public static Builder builder() {
    return new Builder();
  }

  /** Builder for ConnectionSettings with fluent methods. */
  public static final class Builder {
    private TransactionIsolation transactionIsolation = null;
    private Boolean autoCommit = null;
    private Boolean readOnly = null;
    private String catalog = null;
    private String schema = null;
    private String connectionInitSql = null;

    private Builder() {}

    /**
     * Set the transaction isolation level. Default: null (driver default).
     *
     * @param transactionIsolation isolation level
     * @return this builder
     */
    public Builder transactionIsolation(TransactionIsolation transactionIsolation) {
      this.transactionIsolation = transactionIsolation;
      return this;
    }

    /**
     * Set the auto-commit mode. Default: null (driver default, usually true).
     *
     * @param autoCommit auto-commit mode
     * @return this builder
     */
    public Builder autoCommit(boolean autoCommit) {
      this.autoCommit = autoCommit;
      return this;
    }

    /**
     * Set the read-only mode. Default: null (driver default, usually false).
     *
     * @param readOnly read-only mode
     * @return this builder
     */
    public Builder readOnly(boolean readOnly) {
      this.readOnly = readOnly;
      return this;
    }

    /**
     * Set the catalog. Default: null (driver default).
     *
     * @param catalog catalog name
     * @return this builder
     */
    public Builder catalog(String catalog) {
      this.catalog = catalog;
      return this;
    }

    /**
     * Set the schema. Default: null (driver default).
     *
     * @param schema schema name
     * @return this builder
     */
    public Builder schema(String schema) {
      this.schema = schema;
      return this;
    }

    /**
     * SQL to execute when a connection is created. Default: null.
     *
     * @param connectionInitSql initialization SQL
     * @return this builder
     */
    public Builder connectionInitSql(String connectionInitSql) {
      this.connectionInitSql = connectionInitSql;
      return this;
    }

    /** Build the ConnectionSettings. */
    public ConnectionSettings build() {
      return new ConnectionSettings(
          transactionIsolation, autoCommit, readOnly, catalog, schema, connectionInitSql);
    }
  }
}
