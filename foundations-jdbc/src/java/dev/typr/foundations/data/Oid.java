package dev.typr.foundations.data;

/**
 * Wrapper for PostgreSQL OID (Object Identifier) type.
 *
 * <p>OID is a 32-bit unsigned integer used internally by PostgreSQL as a primary key for various
 * system tables. Since Java's int is signed, we use long to properly represent the full range of
 * OID values (0 to 2^32-1).
 */
public record Oid(long value) {
  public static Oid parse(String value) {
    return new Oid(Long.parseLong(value));
  }

  public Oid(String value) {
    this(Oid.parse(value).value);
  }

  @Override
  public String toString() {
    return Long.toString(value);
  }
}
