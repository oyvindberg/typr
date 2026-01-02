package dev.typr.foundations.data;

/** Unsigned 4-byte integer (0-4294967295). Used for MariaDB INT UNSIGNED and MEDIUMINT UNSIGNED. */
public record Uint4(long value) {
  public static final long MIN_VALUE = 0L;
  public static final long MAX_VALUE = 4294967295L;

  public Uint4 {
    if (value < MIN_VALUE || value > MAX_VALUE) {
      throw new IllegalArgumentException(
          "Uint4 value must be between " + MIN_VALUE + " and " + MAX_VALUE + ", got: " + value);
    }
  }

  public static Uint4 of(long value) {
    return new Uint4(value);
  }
}
