package dev.typr.foundations.data;

/** Unsigned 1-byte integer (0-255). Used for SQL Server TINYINT and MariaDB TINYINT UNSIGNED. */
public record Uint1(short value) {
  public static final short MIN_VALUE = 0;
  public static final short MAX_VALUE = 255;

  public Uint1 {
    if (value < MIN_VALUE || value > MAX_VALUE) {
      throw new IllegalArgumentException(
          "Uint1 value must be between " + MIN_VALUE + " and " + MAX_VALUE + ", got: " + value);
    }
  }

  public static Uint1 of(int value) {
    return new Uint1((short) value);
  }
}
