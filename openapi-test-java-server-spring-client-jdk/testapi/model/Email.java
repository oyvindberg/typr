package testapi.model;

import com.fasterxml.jackson.annotation.JsonValue;

/** Email address wrapper */
public record Email(@JsonValue String value) {
  public Email withValue(String value) {
    return new Email(value);
  };

  @Override
  public String toString() {
    return value;
  };

  /** Converts a String to this type for Spring @PathVariable binding */
  static public Email valueOf(String s) {
    return new Email(s);
  };
}