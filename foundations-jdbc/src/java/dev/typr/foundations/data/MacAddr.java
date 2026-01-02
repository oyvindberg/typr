package dev.typr.foundations.data;

public record MacAddr(String value) {
  public String toString() {
    return value;
  }
}
