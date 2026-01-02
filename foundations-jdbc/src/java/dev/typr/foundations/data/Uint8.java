package dev.typr.foundations.data;

import java.math.BigInteger;

/** Unsigned 8-byte integer (0-18446744073709551615). Used for MariaDB BIGINT UNSIGNED. */
public record Uint8(BigInteger value) {
  public static final BigInteger MIN_VALUE = BigInteger.ZERO;
  public static final BigInteger MAX_VALUE = new BigInteger("18446744073709551615");

  public Uint8 {
    if (value.compareTo(MIN_VALUE) < 0 || value.compareTo(MAX_VALUE) > 0) {
      throw new IllegalArgumentException(
          "Uint8 value must be between " + MIN_VALUE + " and " + MAX_VALUE + ", got: " + value);
    }
  }

  public static Uint8 of(long value) {
    return new Uint8(BigInteger.valueOf(value));
  }

  public static Uint8 of(BigInteger value) {
    return new Uint8(value);
  }
}
