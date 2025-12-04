package testapi.model;

import com.fasterxml.jackson.annotation.JsonValue;

/** ISO 4217 currency code */
public record Currency(@JsonValue String value) {
  public Currency withValue(String value) {
    return new Currency(value);
  };

  @Override
  public String toString() {
    return value;
  };

  /** Converts a String to this type for Spring @PathVariable binding */
  static public Currency valueOf(String s) {
    return new Currency(s);
  };
}