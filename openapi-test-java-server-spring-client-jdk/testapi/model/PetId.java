package testapi.model;

import com.fasterxml.jackson.annotation.JsonValue;

/** Unique pet identifier */
public record PetId(@JsonValue String value) {
  public PetId withValue(String value) {
    return new PetId(value);
  };

  @Override
  public String toString() {
    return value;
  };

  /** Converts a String to this type for Spring @PathVariable binding */
  static public PetId valueOf(String s) {
    return new PetId(s);
  };
}