package dev.typr.foundations.data;

public record Cidr(String value) {
  public String toString() {
    return value;
  }
}
