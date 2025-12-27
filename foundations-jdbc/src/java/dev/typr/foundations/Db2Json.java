package dev.typr.foundations;

import dev.typr.foundations.data.JsonValue;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Function;

/**
 * Encodes/decodes values to/from JSON for IBM DB2.
 *
 * <p>Similar to SqlServerJson - DB2 supports JSON natively since version 11.1.
 */
public abstract class Db2Json<A> implements DbJson<A> {
  public abstract JsonValue toJson(A a);

  public abstract A fromJson(JsonValue jsonValue);

  public <B> Db2Json<B> bimap(SqlFunction<A, B> f, Function<B, A> g) {
    var self = this;
    return Db2Json.instance(a -> self.toJson(g.apply(a)), jv -> f.apply(self.fromJson(jv)));
  }

  public <B> Db2Json<B> map(SqlFunction<A, B> f) {
    return bimap(f, null); // write not supported
  }

  public <B> Db2Json<B> contramap(Function<B, A> g) {
    return bimap(null, g); // read not supported
  }

  public Db2Json<Optional<A>> opt() {
    var self = this;
    return instance(
        a -> a.map(self::toJson).orElse(JsonValue.JNull.INSTANCE),
        jv -> jv instanceof JsonValue.JNull ? Optional.empty() : Optional.of(self.fromJson(jv)));
  }

  public static <A> Db2Json<A> instance(
      Function<A, JsonValue> toJson, SqlFunction<JsonValue, A> fromJson) {
    return new Db2Json<>() {
      @Override
      public JsonValue toJson(A a) {
        return toJson.apply(a);
      }

      @Override
      public A fromJson(JsonValue jsonValue) {
        try {
          return fromJson.apply(jsonValue);
        } catch (java.sql.SQLException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  // Standard JSON codecs
  public static final Db2Json<String> text =
      instance(s -> new JsonValue.JString(s), jv -> ((JsonValue.JString) jv).value());
  public static final Db2Json<Boolean> bool =
      instance(JsonValue.JBool::of, jv -> ((JsonValue.JBool) jv).value());
  public static final Db2Json<Short> int2 =
      instance(
          s -> JsonValue.JNumber.of(s.intValue()),
          jv -> Short.parseShort(((JsonValue.JNumber) jv).value()));
  public static final Db2Json<Integer> int4 =
      instance(
          i -> JsonValue.JNumber.of(i.longValue()),
          jv -> Integer.parseInt(((JsonValue.JNumber) jv).value()));
  public static final Db2Json<Long> int8 =
      instance(JsonValue.JNumber::of, jv -> Long.parseLong(((JsonValue.JNumber) jv).value()));
  public static final Db2Json<Float> float4 =
      instance(
          f -> JsonValue.JNumber.of(f.doubleValue()),
          jv -> Float.parseFloat(((JsonValue.JNumber) jv).value()));
  public static final Db2Json<Double> float8 =
      instance(JsonValue.JNumber::of, jv -> Double.parseDouble(((JsonValue.JNumber) jv).value()));
  public static final Db2Json<BigDecimal> numeric =
      instance(
          bd -> JsonValue.JNumber.of(bd.toString()),
          jv -> new BigDecimal(((JsonValue.JNumber) jv).value()));
  public static final Db2Json<byte[]> bytea =
      instance(
          bytes -> new JsonValue.JString(java.util.Base64.getEncoder().encodeToString(bytes)),
          jv -> java.util.Base64.getDecoder().decode(((JsonValue.JString) jv).value()));
  public static final Db2Json<Object> unknown = instance(obj -> (JsonValue) obj, jv -> jv);

  /**
   * Creates a Db2Json that throws UnsupportedOperationException for types that DB2's JSON_OBJECT
   * doesn't support (GRAPHIC, VARGRAPHIC, DBCLOB, BINARY, VARBINARY).
   */
  public static <A> Db2Json<A> unsupported(String typeName) {
    return new Db2Json<>() {
      @Override
      public JsonValue toJson(A a) {
        throw new UnsupportedOperationException(
            "DB2 JSON_OBJECT does not support " + typeName + " type");
      }

      @Override
      public A fromJson(JsonValue jsonValue) {
        throw new UnsupportedOperationException(
            "DB2 JSON_OBJECT does not support " + typeName + " type");
      }
    };
  }

  // Date/Time codecs
  // DB2 JSON_OBJECT uses non-standard formats: TIME as "HH.mm.ss", TIMESTAMP as
  // "yyyy-MM-dd-HH.mm.ss.SSSSSS"
  // We need to handle both ISO format (from in-memory roundtrip) and DB2 format (from database)
  private static final java.time.format.DateTimeFormatter DB2_TIME =
      java.time.format.DateTimeFormatter.ofPattern("HH.mm.ss");
  private static final java.time.format.DateTimeFormatter DB2_TIMESTAMP =
      java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS");

  public static final Db2Json<java.time.LocalDate> date =
      instance(
          d -> new JsonValue.JString(d.toString()),
          jv -> java.time.LocalDate.parse(((JsonValue.JString) jv).value()));

  public static final Db2Json<java.time.LocalTime> time =
      instance(
          t -> new JsonValue.JString(t.toString()),
          jv -> {
            String s = ((JsonValue.JString) jv).value();
            // DB2 format uses dots (14.30.45), ISO uses colons (14:30:45)
            if (s.contains(":")) {
              return java.time.LocalTime.parse(s);
            }
            return java.time.LocalTime.parse(s, DB2_TIME);
          });

  public static final Db2Json<java.time.LocalDateTime> timestamp =
      instance(
          ts -> new JsonValue.JString(ts.toString()),
          jv -> {
            String s = ((JsonValue.JString) jv).value();
            // DB2 format: "yyyy-MM-dd-HH.mm.ss.SSSSSS", ISO format: "yyyy-MM-ddTHH:mm:ss..."
            if (s.contains("T")) {
              return java.time.LocalDateTime.parse(s);
            }
            return java.time.LocalDateTime.parse(s, DB2_TIMESTAMP);
          });

  public static final Db2Json<java.time.OffsetDateTime> timestamptz =
      instance(
          odt -> new JsonValue.JString(odt.toString()),
          jv -> java.time.OffsetDateTime.parse(((JsonValue.JString) jv).value()));
}
