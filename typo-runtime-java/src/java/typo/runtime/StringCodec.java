package typo.runtime;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.function.Function;

/**
 * Encodes and decodes values to/from String representation.
 * Used for:
 * - String-based list/map readers (when JDBC returns strings for nested types)
 */
public record StringCodec<A>(
        Function<A, String> encode,
        Function<String, A> decode
) {
    public <B> StringCodec<B> bimap(Function<A, B> decodeB, Function<B, A> encodeB) {
        return new StringCodec<>(
                b -> encode.apply(encodeB.apply(b)),
                s -> decodeB.apply(decode.apply(s))
        );
    }

    // ==================== Primitive types ====================

    public static final StringCodec<String> string = new StringCodec<>(
            Function.identity(),
            Function.identity()
    );

    public static final StringCodec<Boolean> bool = new StringCodec<>(
            b -> b ? "true" : "false",
            Boolean::parseBoolean
    );

    public static final StringCodec<Byte> int1 = new StringCodec<>(
            Object::toString,
            Byte::parseByte
    );

    public static final StringCodec<Short> int2 = new StringCodec<>(
            Object::toString,
            Short::parseShort
    );

    public static final StringCodec<Integer> int4 = new StringCodec<>(
            Object::toString,
            Integer::parseInt
    );

    public static final StringCodec<Long> int8 = new StringCodec<>(
            Object::toString,
            Long::parseLong
    );

    public static final StringCodec<Float> float4 = new StringCodec<>(
            Object::toString,
            Float::parseFloat
    );

    public static final StringCodec<Double> float8 = new StringCodec<>(
            Object::toString,
            Double::parseDouble
    );

    public static final StringCodec<BigDecimal> numeric = new StringCodec<>(
            BigDecimal::toString,
            BigDecimal::new
    );

    public static final StringCodec<BigInteger> hugeint = new StringCodec<>(
            BigInteger::toString,
            BigInteger::new
    );

    // ==================== Binary types ====================

    public static final StringCodec<byte[]> blob = new StringCodec<>(
            bytes -> {
                StringBuilder sb = new StringBuilder("\\x");
                for (byte b : bytes) {
                    sb.append(String.format("%02x", b));
                }
                return sb.toString();
            },
            s -> {
                if (s.startsWith("\\x")) s = s.substring(2);
                byte[] bytes = new byte[s.length() / 2];
                for (int i = 0; i < bytes.length; i++) {
                    bytes[i] = (byte) Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16);
                }
                return bytes;
            }
    );

    // ==================== Date/Time types ====================

    public static final StringCodec<LocalDate> date = new StringCodec<>(
            LocalDate::toString,
            LocalDate::parse
    );

    public static final StringCodec<LocalTime> time = new StringCodec<>(
            LocalTime::toString,
            LocalTime::parse
    );

    public static final StringCodec<LocalDateTime> timestamp = new StringCodec<>(
            LocalDateTime::toString,
            LocalDateTime::parse
    );

    public static final StringCodec<OffsetDateTime> timestamptz = new StringCodec<>(
            OffsetDateTime::toString,
            OffsetDateTime::parse
    );

    public static final StringCodec<Duration> interval = new StringCodec<>(
            d -> String.format("%02d:%02d:%02d", d.toHours(), d.toMinutesPart(), d.toSecondsPart()),
            s -> {
                // Parse HH:MM:SS or HH:MM:SS.micros format
                String[] parts = s.split(":");
                if (parts.length >= 3) {
                    long hours = Long.parseLong(parts[0]);
                    long minutes = Long.parseLong(parts[1]);
                    String secPart = parts[2];
                    int dotIdx = secPart.indexOf('.');
                    long seconds;
                    long nanos = 0;
                    if (dotIdx >= 0) {
                        seconds = Long.parseLong(secPart.substring(0, dotIdx));
                        String fracStr = secPart.substring(dotIdx + 1);
                        while (fracStr.length() < 9) fracStr += "0";
                        if (fracStr.length() > 9) fracStr = fracStr.substring(0, 9);
                        nanos = Long.parseLong(fracStr);
                    } else {
                        seconds = Long.parseLong(secPart);
                    }
                    return Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds).plusNanos(nanos);
                }
                return Duration.parse(s);
            }
    );

    // ==================== UUID ====================

    public static final StringCodec<UUID> uuid = new StringCodec<>(
            UUID::toString,
            UUID::fromString
    );

    // ==================== Bit string ====================

    public static final StringCodec<String> bit = string; // BIT is already a string of 0s and 1s

    // ==================== Passthrough (for types that don't need conversion) ====================

    /**
     * Create a passthrough codec that uses toString() for encoding.
     * This is useful for primitive types (Integer, Long, etc.) that can be written
     * directly by JDBC but need to be converted to String for composite type wire formats.
     * <p>
     * Note: The decode function just returns the string - the actual type conversion
     * is handled by JDBC when reading.
     */
    @SuppressWarnings("unchecked")
    public static <T> StringCodec<T> passthrough() {
        return (StringCodec<T>) PASSTHROUGH;
    }

    private static final StringCodec<Object> PASSTHROUGH = new StringCodec<>(
            Object::toString,
            s -> s // Returns the string; actual type conversion is done elsewhere
    );
}
