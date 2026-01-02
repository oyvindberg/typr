package dev.typr.foundations;

import dev.typr.foundations.data.Json;
import dev.typr.foundations.data.Uint1;
import dev.typr.foundations.data.Uint2;
import dev.typr.foundations.data.Uint4;
import dev.typr.foundations.data.Uint8;
import dev.typr.foundations.data.maria.Inet4;
import dev.typr.foundations.data.maria.Inet6;
import dev.typr.foundations.data.maria.MariaSet;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.util.function.Function;
import org.mariadb.jdbc.type.Geometry;
import org.mariadb.jdbc.type.GeometryCollection;
import org.mariadb.jdbc.type.LineString;
import org.mariadb.jdbc.type.MultiLineString;
import org.mariadb.jdbc.type.MultiPoint;
import org.mariadb.jdbc.type.MultiPolygon;
import org.mariadb.jdbc.type.Point;
import org.mariadb.jdbc.type.Polygon;

/**
 * MariaDB type definitions for the typr-runtime-java library.
 *
 * <p>This interface provides type codecs for all MariaDB data types, similar to PgTypes for
 * PostgreSQL.
 */
public interface MariaTypes {
  // ==================== Integer Types (Signed) ====================

  MariaType<Byte> tinyint =
      MariaType.of(
          "TINYINT",
          MariaRead.readByte,
          MariaWrite.writeByte,
          MariaText.textByte,
          MariaJson.int4.bimap(Integer::byteValue, Byte::intValue));

  MariaType<Short> smallint =
      MariaType.of(
          "SMALLINT",
          MariaRead.readShort,
          MariaWrite.writeShort,
          MariaText.textShort,
          MariaJson.int2);

  MariaType<Integer> mediumint =
      MariaType.of(
          "MEDIUMINT",
          MariaRead.readInteger,
          MariaWrite.writeInteger,
          MariaText.textInteger,
          MariaJson.int4);

  MariaType<Integer> int_ =
      MariaType.of(
          "INT",
          MariaRead.readInteger,
          MariaWrite.writeInteger,
          MariaText.textInteger,
          MariaJson.int4);

  MariaType<Long> bigint =
      MariaType.of(
          "BIGINT", MariaRead.readLong, MariaWrite.writeLong, MariaText.textLong, MariaJson.int8);

  // ==================== Integer Types (Unsigned) ====================

  // TINYINT UNSIGNED: 0-255, wrapped in Uint1
  MariaType<Uint1> tinyintUnsigned =
      MariaType.of(
          "TINYINT UNSIGNED",
          MariaRead.readShort.map(Uint1::new),
          MariaWrite.writeShort.contramap(Uint1::value),
          MariaText.textShort.contramap(Uint1::value),
          MariaJson.int2.bimap(Uint1::new, Uint1::value));

  // SMALLINT UNSIGNED: 0-65535, wrapped in Uint2
  MariaType<Uint2> smallintUnsigned =
      MariaType.of(
          "SMALLINT UNSIGNED",
          MariaRead.readInteger.map(Uint2::new),
          MariaWrite.writeInteger.contramap(Uint2::value),
          MariaText.textInteger.contramap(Uint2::value),
          MariaJson.int4.bimap(Uint2::new, Uint2::value));

  // MEDIUMINT UNSIGNED: 0-16777215, wrapped in Uint4
  MariaType<Uint4> mediumintUnsigned =
      MariaType.of(
          "MEDIUMINT UNSIGNED",
          MariaRead.readLong.map(Uint4::new),
          MariaWrite.writeLong.contramap(Uint4::value),
          MariaText.textLong.contramap(Uint4::value),
          MariaJson.int8.bimap(Uint4::new, Uint4::value));

  // INT UNSIGNED: 0-4294967295, wrapped in Uint4
  MariaType<Uint4> intUnsigned =
      MariaType.of(
          "INT UNSIGNED",
          MariaRead.readLong.map(Uint4::new),
          MariaWrite.writeLong.contramap(Uint4::value),
          MariaText.textLong.contramap(Uint4::value),
          MariaJson.int8.bimap(Uint4::new, Uint4::value));

  // BIGINT UNSIGNED: 0-18446744073709551615, wrapped in Uint8
  MariaType<Uint8> bigintUnsigned =
      MariaType.of(
          "BIGINT UNSIGNED",
          MariaRead.readBigInteger.map(Uint8::new),
          MariaWrite.writeBigInteger.contramap(Uint8::value),
          MariaText.textBigInteger.contramap(Uint8::value),
          MariaJson.numeric.bimap(
              v -> new Uint8(v.toBigInteger()), v -> new BigDecimal(v.value())));

  // ==================== Fixed-Point Types ====================

  MariaType<BigDecimal> decimal =
      MariaType.of(
          "DECIMAL",
          MariaRead.readBigDecimal,
          MariaWrite.writeBigDecimal,
          MariaText.textBigDecimal,
          MariaJson.numeric);

  MariaType<BigDecimal> numeric = decimal.renamed("NUMERIC");

  static MariaType<BigDecimal> decimal(int precision, int scale) {
    return MariaType.of(
        MariaTypename.of("DECIMAL", precision, scale),
        MariaRead.readBigDecimal,
        MariaWrite.writeBigDecimal,
        MariaText.textBigDecimal,
        MariaJson.numeric);
  }

  // ==================== Floating-Point Types ====================

  MariaType<Float> float_ =
      MariaType.of(
          "FLOAT",
          MariaRead.readFloat,
          MariaWrite.writeFloat,
          MariaText.textFloat,
          MariaJson.float4);

  MariaType<Double> double_ =
      MariaType.of(
          "DOUBLE",
          MariaRead.readDouble,
          MariaWrite.writeDouble,
          MariaText.textDouble,
          MariaJson.float8);

  // ==================== Boolean Type ====================

  MariaType<Boolean> bool =
      MariaType.of(
          "BOOLEAN",
          MariaRead.readBoolean,
          MariaWrite.writeBoolean,
          MariaText.textBoolean,
          MariaJson.bool);

  // ==================== Bit Types ====================

  // BIT(1) as Boolean
  MariaType<Boolean> bit1 =
      MariaType.of(
          "BIT",
          MariaRead.readBitAsBoolean,
          MariaWrite.writeBoolean,
          MariaText.textBoolean,
          MariaJson.bool);

  // BIT(n) as byte[]
  MariaType<byte[]> bit =
      MariaType.of(
          "BIT",
          MariaRead.readBit,
          MariaWrite.writeByteArray,
          MariaText.textByteArray,
          MariaJson.bytea);

  // ==================== String Types ====================

  MariaType<String> char_ =
      MariaType.of(
          "CHAR",
          MariaRead.readString,
          MariaWrite.writeString,
          MariaText.textString,
          MariaJson.text);

  MariaType<String> varchar =
      MariaType.of(
          "VARCHAR",
          MariaRead.readString,
          MariaWrite.writeString,
          MariaText.textString,
          MariaJson.text);

  MariaType<String> tinytext =
      MariaType.of(
          "TINYTEXT",
          MariaRead.readString,
          MariaWrite.writeString,
          MariaText.textString,
          MariaJson.text);

  MariaType<String> text =
      MariaType.of(
          "TEXT",
          MariaRead.readString,
          MariaWrite.writeString,
          MariaText.textString,
          MariaJson.text);

  MariaType<String> mediumtext =
      MariaType.of(
          "MEDIUMTEXT",
          MariaRead.readString,
          MariaWrite.writeString,
          MariaText.textString,
          MariaJson.text);

  MariaType<String> longtext =
      MariaType.of(
          "LONGTEXT",
          MariaRead.readString,
          MariaWrite.writeString,
          MariaText.textString,
          MariaJson.text);

  static MariaType<String> char_(int length) {
    return MariaType.of(
        MariaTypename.of("CHAR", length),
        MariaRead.readString,
        MariaWrite.writeString,
        MariaText.textString,
        MariaJson.text);
  }

  static MariaType<String> varchar(int length) {
    return MariaType.of(
        MariaTypename.of("VARCHAR", length),
        MariaRead.readString,
        MariaWrite.writeString,
        MariaText.textString,
        MariaJson.text);
  }

  // ==================== Binary Types ====================

  MariaType<byte[]> binary =
      MariaType.of(
          "BINARY",
          MariaRead.readByteArray,
          MariaWrite.writeByteArray,
          MariaText.textByteArray,
          MariaJson.bytea);

  MariaType<byte[]> varbinary =
      MariaType.of(
          "VARBINARY",
          MariaRead.readByteArray,
          MariaWrite.writeByteArray,
          MariaText.textByteArray,
          MariaJson.bytea);

  MariaType<byte[]> tinyblob =
      MariaType.of(
          "TINYBLOB",
          MariaRead.readBlob,
          MariaWrite.writeByteArray,
          MariaText.textByteArray,
          MariaJson.bytea);

  MariaType<byte[]> blob =
      MariaType.of(
          "BLOB",
          MariaRead.readBlob,
          MariaWrite.writeByteArray,
          MariaText.textByteArray,
          MariaJson.bytea);

  MariaType<byte[]> mediumblob =
      MariaType.of(
          "MEDIUMBLOB",
          MariaRead.readBlob,
          MariaWrite.writeByteArray,
          MariaText.textByteArray,
          MariaJson.bytea);

  MariaType<byte[]> longblob =
      MariaType.of(
          "LONGBLOB",
          MariaRead.readBlob,
          MariaWrite.writeByteArray,
          MariaText.textByteArray,
          MariaJson.bytea);

  static MariaType<byte[]> binary(int length) {
    return MariaType.of(
        MariaTypename.of("BINARY", length),
        MariaRead.readByteArray,
        MariaWrite.writeByteArray,
        MariaText.textByteArray,
        MariaJson.bytea);
  }

  static MariaType<byte[]> varbinary(int length) {
    return MariaType.of(
        MariaTypename.of("VARBINARY", length),
        MariaRead.readByteArray,
        MariaWrite.writeByteArray,
        MariaText.textByteArray,
        MariaJson.bytea);
  }

  // ==================== Date/Time Types ====================

  MariaType<LocalDate> date =
      MariaType.of(
          "DATE",
          MariaRead.readLocalDate,
          MariaWrite.passObjectToJdbc(),
          MariaText.instanceToString(),
          MariaJson.date);

  MariaType<LocalTime> time =
      MariaType.of(
          "TIME",
          MariaRead.readLocalTime,
          MariaWrite.passObjectToJdbc(),
          MariaText.instanceToString(),
          MariaJson.time);

  MariaType<LocalDateTime> datetime =
      MariaType.of(
          "DATETIME",
          MariaRead.readLocalDateTime,
          MariaWrite.passObjectToJdbc(),
          MariaText.instanceToString(),
          MariaJson.timestamp);

  MariaType<LocalDateTime> timestamp =
      MariaType.of(
          "TIMESTAMP",
          MariaRead.readLocalDateTime,
          MariaWrite.passObjectToJdbc(),
          MariaText.instanceToString(),
          MariaJson.timestamp);

  MariaType<Year> year =
      MariaType.of(
          "YEAR",
          MariaRead.readYear,
          MariaWrite.writeShort.contramap(y -> (short) y.getValue()),
          MariaText.textInteger.contramap(Year::getValue),
          MariaJson.int4.bimap(Year::of, Year::getValue));

  static MariaType<LocalTime> time(int fsp) {
    return MariaType.of(
        MariaTypename.of("TIME", fsp),
        MariaRead.readLocalTime,
        MariaWrite.passObjectToJdbc(),
        MariaText.instanceToString(),
        MariaJson.time);
  }

  static MariaType<LocalDateTime> datetime(int fsp) {
    return MariaType.of(
        MariaTypename.of("DATETIME", fsp),
        MariaRead.readLocalDateTime,
        MariaWrite.passObjectToJdbc(),
        MariaText.instanceToString(),
        MariaJson.timestamp);
  }

  static MariaType<LocalDateTime> timestamp(int fsp) {
    return MariaType.of(
        MariaTypename.of("TIMESTAMP", fsp),
        MariaRead.readLocalDateTime,
        MariaWrite.passObjectToJdbc(),
        MariaText.instanceToString(),
        MariaJson.timestamp);
  }

  // ==================== ENUM Type ====================

  /**
   * Create a MariaType for ENUM columns. MariaDB ENUMs are read/written as strings.
   *
   * @param fromString function to convert string to enum value
   * @param <E> the enum type
   * @return MariaType for the enum
   */
  static <E extends Enum<E>> MariaType<E> ofEnum(String sqlType, Function<String, E> fromString) {
    return MariaType.of(
        sqlType,
        MariaRead.readString.map(fromString::apply),
        MariaWrite.writeString.contramap(Enum::name),
        MariaText.textString.contramap(Enum::name),
        MariaJson.text.bimap(fromString::apply, Enum::name));
  }

  // ==================== SET Type ====================

  /** MariaSet wrapper for SET columns. */
  MariaType<MariaSet> set =
      MariaType.of(
          "SET",
          MariaRead.readString.map(MariaSet::fromString),
          MariaWrite.writeString.contramap(MariaSet::toCommaSeparated),
          MariaText.textString.contramap(MariaSet::toCommaSeparated),
          MariaJson.text.bimap(MariaSet::fromString, MariaSet::toCommaSeparated));

  // ==================== JSON Type ====================

  /** JSON type - reuses dev.typr.foundations.data.Json from the common types. */
  MariaType<Json> json =
      MariaType.of(
          "JSON",
          MariaRead.readString.map(Json::new),
          MariaWrite.writeString.contramap(Json::value),
          MariaText.textString.contramap(Json::value),
          MariaJson.json);

  // ==================== Network Types ====================

  MariaType<Inet4> inet4 =
      MariaType.of(
          "INET4",
          MariaRead.readString.map(Inet4::parse),
          MariaWrite.writeString.contramap(Inet4::value),
          MariaText.textString.contramap(Inet4::value),
          MariaJson.text.bimap(Inet4::parse, Inet4::value));

  MariaType<Inet6> inet6 =
      MariaType.of(
          "INET6",
          MariaRead.readString.map(Inet6::parse),
          MariaWrite.writeString.contramap(Inet6::value),
          MariaText.textString.contramap(Inet6::value),
          MariaJson.text.bimap(Inet6::parse, Inet6::value));

  // ==================== Spatial Types ====================
  // Using MariaDB Connector/J types directly.
  // We use readGeometry() which handles both normal SELECT (returns typed Geometry objects)
  // and RETURNING clauses (returns WKB bytes). For base Geometry type, it parses the WKB
  // header to determine the actual geometry type and uses the appropriate codec.
  // Note: Spatial types use text representation for JSON (WKT format would be better but is
  // complex)

  MariaType<Geometry> geometry =
      MariaType.of(
          "GEOMETRY",
          MariaRead.readGeometry(Geometry.class),
          MariaWrite.passObjectToJdbc(),
          MariaText.NotWorking(),
          MariaJson.text.bimap(
              s -> {
                throw new UnsupportedOperationException("Geometry JSON not supported");
              },
              Object::toString));

  MariaType<Point> point =
      MariaType.of(
          "POINT",
          MariaRead.readGeometry(Point.class),
          MariaWrite.passObjectToJdbc(),
          MariaText.NotWorking(),
          MariaJson.text.bimap(
              s -> {
                throw new UnsupportedOperationException("Point JSON not supported");
              },
              Object::toString));

  MariaType<LineString> linestring =
      MariaType.of(
          "LINESTRING",
          MariaRead.readGeometry(LineString.class),
          MariaWrite.passObjectToJdbc(),
          MariaText.NotWorking(),
          MariaJson.text.bimap(
              s -> {
                throw new UnsupportedOperationException("LineString JSON not supported");
              },
              Object::toString));

  MariaType<Polygon> polygon =
      MariaType.of(
          "POLYGON",
          MariaRead.readGeometry(Polygon.class),
          MariaWrite.passObjectToJdbc(),
          MariaText.NotWorking(),
          MariaJson.text.bimap(
              s -> {
                throw new UnsupportedOperationException("Polygon JSON not supported");
              },
              Object::toString));

  MariaType<MultiPoint> multipoint =
      MariaType.of(
          "MULTIPOINT",
          MariaRead.readGeometry(MultiPoint.class),
          MariaWrite.passObjectToJdbc(),
          MariaText.NotWorking(),
          MariaJson.text.bimap(
              s -> {
                throw new UnsupportedOperationException("MultiPoint JSON not supported");
              },
              Object::toString));

  MariaType<MultiLineString> multilinestring =
      MariaType.of(
          "MULTILINESTRING",
          MariaRead.readGeometry(MultiLineString.class),
          MariaWrite.passObjectToJdbc(),
          MariaText.NotWorking(),
          MariaJson.text.bimap(
              s -> {
                throw new UnsupportedOperationException("MultiLineString JSON not supported");
              },
              Object::toString));

  MariaType<MultiPolygon> multipolygon =
      MariaType.of(
          "MULTIPOLYGON",
          MariaRead.readGeometry(MultiPolygon.class),
          MariaWrite.passObjectToJdbc(),
          MariaText.NotWorking(),
          MariaJson.text.bimap(
              s -> {
                throw new UnsupportedOperationException("MultiPolygon JSON not supported");
              },
              Object::toString));

  MariaType<GeometryCollection> geometrycollection =
      MariaType.of(
          "GEOMETRYCOLLECTION",
          MariaRead.readGeometry(GeometryCollection.class),
          MariaWrite.passObjectToJdbc(),
          MariaText.NotWorking(),
          MariaJson.text.bimap(
              s -> {
                throw new UnsupportedOperationException("GeometryCollection JSON not supported");
              },
              Object::toString));

  // ==================== Unknown Type ====================
  // For columns whose type typr doesn't know how to handle - cast to/from string
  MariaType<dev.typr.foundations.data.Unknown> unknown =
      MariaType.of(
              "TEXT",
              MariaRead.readString,
              MariaWrite.writeString,
              MariaText.textString,
              MariaJson.text)
          .bimap(dev.typr.foundations.data.Unknown::new, dev.typr.foundations.data.Unknown::value);
}
