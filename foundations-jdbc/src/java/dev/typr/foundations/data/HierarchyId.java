package dev.typr.foundations.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for SQL Server's HIERARCHYID type. Stores hierarchical path segments and provides
 * conversion to/from the canonical string path format (e.g., "/1/2/3/") and binary representation.
 */
public record HierarchyId(List<Long> segments) {

  /** The root hierarchy node "/" */
  public static final HierarchyId ROOT = new HierarchyId(List.of());

  public HierarchyId {
    segments = List.copyOf(segments);
  }

  /**
   * Create a HierarchyId from binary representation (as returned by SQL Server).
   *
   * @param bytes The binary representation
   * @return The HierarchyId instance
   */
  public static HierarchyId fromBytes(byte[] bytes) {
    if (bytes == null || bytes.length == 0) {
      return ROOT;
    }
    return new HierarchyId(decodeBytes(bytes));
  }

  /**
   * Parse a hierarchyid from its canonical string representation.
   *
   * @param path The path string like "/", "/1/", "/1/2/3/"
   * @return The HierarchyId instance
   */
  public static HierarchyId parse(String path) {
    if (path == null || path.isEmpty() || path.equals("/")) {
      return ROOT;
    }

    List<Long> segments = new ArrayList<>();
    String[] parts = path.split("/");
    for (String part : parts) {
      if (!part.isEmpty()) {
        segments.add(Long.parseLong(part));
      }
    }

    if (segments.isEmpty()) {
      return ROOT;
    }

    return new HierarchyId(segments);
  }

  /**
   * Convert this HierarchyId to its canonical string representation.
   *
   * @return The path string like "/", "/1/", "/1/2/3/"
   */
  @Override
  public String toString() {
    if (segments.isEmpty()) {
      return "/";
    }

    StringBuilder result = new StringBuilder("/");
    for (Long segment : segments) {
      result.append(segment).append("/");
    }
    return result.toString();
  }

  // ==================== Binary Decoding ====================

  private static List<Long> decodeBytes(byte[] bytes) {
    List<Long> result = new ArrayList<>();
    BitReader reader = new BitReader(bytes);

    while (reader.hasMore()) {
      Long label = decodeLabel(reader);
      if (label == null) {
        break;
      }
      result.add(label);
    }

    return result;
  }

  private static Long decodeLabel(BitReader reader) {
    // Count leading zeros to determine type
    int zeros = 0;
    while (reader.hasMore() && reader.peekBit() == 0) {
      reader.readBit();
      zeros++;
      if (zeros > 10) {
        return null; // Too many zeros, probably padding
      }
    }

    if (!reader.hasMore()) {
      return null;
    }

    // Read the '1' that ends the prefix
    int one = reader.readBit();
    if (one != 1) {
      return null;
    }

    // Decode based on type (number of zeros before the 1)
    return switch (zeros) {
      case 0 -> null; // Type O1: fake/fractional nodes (not common)
      case 1 -> { // Type O2: 01 prefix, 2 value bits, values 0-3
        if (!reader.hasBits(3)) yield null;
        long val = reader.readBits(2);
        reader.readBit(); // terminator
        yield val;
      }
      case 2 -> { // Type O3: 001 prefix, 2 value bits, values 4-7
        if (!reader.hasBits(3)) yield null;
        long val = reader.readBits(2) + 4;
        reader.readBit(); // terminator
        yield val;
      }
      case 3 -> { // Type O4: 0001 prefix, 3 value bits, values 8-15
        if (!reader.hasBits(4)) yield null;
        long val = reader.readBits(3) + 8;
        reader.readBit(); // terminator
        yield val;
      }
      case 4 -> { // Type O5: 00001 prefix, 6 value bits, values 16-79
        if (!reader.hasBits(7)) yield null;
        long val = reader.readBits(6) + 16;
        reader.readBit(); // terminator
        yield val;
      }
      case 5 -> { // Type O6: 000001 prefix, 10 value bits, values 80-1103
        if (!reader.hasBits(11)) yield null;
        long val = reader.readBits(10) + 80;
        reader.readBit(); // terminator
        yield val;
      }
      case 6 -> { // Type O7: 0000001 prefix, 14 value bits
        if (!reader.hasBits(15)) yield null;
        long val = reader.readBits(14) + 1104;
        reader.readBit(); // terminator
        yield val;
      }
      default -> null;
    };
  }

  // Helper class for reading bits
  private static class BitReader {
    private final byte[] bytes;
    private int bytePos = 0;
    private int bitPos = 7;

    BitReader(byte[] bytes) {
      this.bytes = bytes;
    }

    boolean hasMore() {
      return bytePos < bytes.length;
    }

    boolean hasBits(int n) {
      int totalBitsLeft = (bytes.length - bytePos) * 8 - (7 - bitPos);
      return totalBitsLeft >= n;
    }

    int peekBit() {
      if (!hasMore()) return 0;
      return (bytes[bytePos] >> bitPos) & 1;
    }

    int readBit() {
      if (!hasMore()) return 0;
      int bit = (bytes[bytePos] >> bitPos) & 1;
      bitPos--;
      if (bitPos < 0) {
        bytePos++;
        bitPos = 7;
      }
      return bit;
    }

    long readBits(int numBits) {
      long value = 0;
      for (int i = 0; i < numBits; i++) {
        value = (value << 1) | readBit();
      }
      return value;
    }
  }
}
