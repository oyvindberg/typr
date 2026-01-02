package dev.typr.foundations.data;

/** Unsigned 2-byte integer (0-65535). Used for MariaDB SMALLINT UNSIGNED. */
public record Uint2(int value) {
  public static final int MIN_VALUE = 0;
  public static final int MAX_VALUE = 65535;

  public Uint2 {
    if (value < MIN_VALUE || value > MAX_VALUE) {
      throw new IllegalArgumentException(
          "Uint2 value must be between " + MIN_VALUE + " and " + MAX_VALUE + ", got: " + value);
    }
  }

  public static Uint2 of(int value) {
    return new Uint2(value);
  }
}
