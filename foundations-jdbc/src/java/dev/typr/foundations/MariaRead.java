package dev.typr.foundations;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.util.Optional;

/**
 * Describes how to read a column from a {@link ResultSet} for MariaDB.
 *
 * <p>Similar to PgRead but adapted for MariaDB-specific types.
 */
public sealed interface MariaRead<A> extends DbRead<A>
    permits MariaRead.NonNullable, MariaRead.Nullable, MariaRead.Mapped {
  A read(ResultSet rs, int col) throws SQLException;

  <B> MariaRead<B> map(SqlFunction<A, B> f);

  /** Derive a MariaRead which allows nullable values */
  MariaRead<Optional<A>> opt();

  @FunctionalInterface
  interface RawRead<A> {
    A apply(ResultSet rs, int column) throws SQLException;
  }

  /**
   * Create an instance of {@link MariaRead} from a function that reads a value from a result set.
   *
   * @param f Should not blow up if the value returned is `null`
   */
  static <A> NonNullable<A> of(RawRead<A> f) {
    RawRead<Optional<A>> readNullableA =
        (rs, col) -> {
          var a = f.apply(rs, col);
          if (rs.wasNull()) return Optional.empty();
          else return Optional.of(a);
        };
    return new NonNullable<>(readNullableA);
  }

  final class NonNullable<A> implements MariaRead<A> {
    final RawRead<Optional<A>> readNullable;

    public NonNullable(RawRead<Optional<A>> readNullable) {
      this.readNullable = readNullable;
    }

    @Override
    public A read(ResultSet rs, int col) throws SQLException {
      return readNullable
          .apply(rs, col)
          .orElseThrow(() -> new SQLException("null value in column " + col));
    }

    @Override
    public <B> NonNullable<B> map(SqlFunction<A, B> f) {
      return new NonNullable<>(
          (rs, col) -> {
            Optional<A> maybeA = readNullable.apply(rs, col);
            if (maybeA.isEmpty()) return Optional.empty();
            return Optional.of(f.apply(maybeA.get()));
          });
    }

    @Override
    public MariaRead<Optional<A>> opt() {
      return new Nullable<>(readNullable);
    }
  }

  final class Nullable<A> implements MariaRead<Optional<A>> {
    final RawRead<Optional<A>> readNullable;

    public Nullable(RawRead<Optional<A>> readNullable) {
      this.readNullable = readNullable;
    }

    @Override
    public Optional<A> read(ResultSet rs, int col) throws SQLException {
      return readNullable.apply(rs, col);
    }

    @Override
    public <B> MariaRead<B> map(SqlFunction<Optional<A>, B> f) {
      return new Mapped<>(this, f);
    }

    @Override
    public Nullable<Optional<A>> opt() {
      return new Nullable<>(
          (rs, col) -> {
            Optional<A> maybeA = readNullable.apply(rs, col);
            if (maybeA.isEmpty()) return Optional.empty();
            return Optional.of(maybeA);
          });
    }
  }

  /**
   * A read that came from mapping another read. Just returns whatever the mapping function
   * produces, null or not.
   */
  record Mapped<A, B>(MariaRead<A> underlying, SqlFunction<A, B> f) implements MariaRead<B> {
    @Override
    public B read(ResultSet rs, int col) throws SQLException {
      return f.apply(underlying.read(rs, col));
    }

    @Override
    public <C> MariaRead<C> map(SqlFunction<B, C> g) {
      return new Mapped<>(this, g);
    }

    @Override
    public MariaRead<Optional<B>> opt() {
      return new Nullable<>((rs, col) -> Optional.ofNullable(read(rs, col)));
    }
  }

  static <A> NonNullable<A> castJdbcObjectTo(Class<A> cls) {
    return of((rs, i) -> cls.cast(rs.getObject(i)));
  }

  /**
   * Read a value by requesting a specific class from JDBC. This uses rs.getObject(i, cls) which
   * allows the JDBC driver to do proper type conversion, including handling WKB bytes for spatial
   * types returned from RETURNING clauses.
   */
  static <A> NonNullable<A> getObjectAs(Class<A> cls) {
    return of((rs, i) -> rs.getObject(i, cls));
  }

  /**
   * Read a value by getting the raw Object and casting. This is needed for polymorphic types like
   * MariaDB's Geometry where getObject(i, Geometry.class) doesn't work properly (the driver uses a
   * specific codec like GeometryCollectionCodec instead of handling polymorphism).
   *
   * <p>For RETURNING clauses, MariaDB returns WKB bytes instead of Geometry objects. In that case,
   * we use the driver's codec to decode the bytes.
   */
  @SuppressWarnings("unchecked")
  static <A> NonNullable<A> getObjectAndCast(Class<A> cls) {
    return of(
        (rs, i) -> {
          Object obj = rs.getObject(i);
          if (obj == null) return null;
          if (cls.isInstance(obj)) {
            return (A) obj;
          }
          // For RETURNING clauses, the driver returns WKB bytes - try to decode via typed getObject
          if (obj instanceof byte[]) {
            // The driver can decode WKB bytes if we ask for the specific type
            // For the base Geometry class, we need to determine the actual type from WKB header
            try {
              // Try using the driver's codec system with the target class
              return rs.getObject(i, cls);
            } catch (SQLException e) {
              // If that fails, the caller may need to handle byte[] differently
              throw new SQLException(
                  "Cannot decode WKB bytes as " + cls.getName() + ": " + e.getMessage(), e);
            }
          }
          throw new SQLException(
              "Expected " + cls.getName() + " but got: " + obj.getClass().getName());
        });
  }

  /**
   * Read a geometry value that handles both normal SELECT (returns typed object) and RETURNING
   * (returns WKB bytes). For RETURNING with base Geometry type, we parse the WKB header to
   * determine the actual geometry type.
   */
  @SuppressWarnings("unchecked")
  static <A extends org.mariadb.jdbc.type.Geometry> NonNullable<A> readGeometry(Class<A> cls) {
    return of(
        (rs, i) -> {
          Object obj = rs.getObject(i);
          if (obj == null) return null;

          // If already the right type, return it
          if (cls.isInstance(obj)) {
            return (A) obj;
          }

          // If it's any Geometry subtype and we want base Geometry, return it
          if (cls == org.mariadb.jdbc.type.Geometry.class
              && obj instanceof org.mariadb.jdbc.type.Geometry) {
            return (A) obj;
          }

          // For WKB bytes from RETURNING, try type-specific decode
          if (obj instanceof byte[]) {
            // For specific subtypes, use the driver's codec directly
            if (cls != org.mariadb.jdbc.type.Geometry.class) {
              return rs.getObject(i, cls);
            }

            // For base Geometry, we need to determine type from WKB and use appropriate decoder
            byte[] wkb = (byte[]) obj;
            if (wkb.length < 5) {
              throw new SQLException("WKB data too short");
            }

            // WKB format: 1 byte endian (0=big, 1=little) + 4 bytes type
            // Types: 1=Point, 2=LineString, 3=Polygon, 4=MultiPoint, 5=MultiLineString,
            // 6=MultiPolygon, 7=GeometryCollection
            boolean littleEndian = (wkb[0] == 1);
            int typeOffset = 1; // Skip endian byte

            int wkbType;
            if (littleEndian) {
              wkbType =
                  (wkb[typeOffset] & 0xFF)
                      | ((wkb[typeOffset + 1] & 0xFF) << 8)
                      | ((wkb[typeOffset + 2] & 0xFF) << 16)
                      | ((wkb[typeOffset + 3] & 0xFF) << 24);
            } else {
              // Big-endian
              wkbType =
                  ((wkb[typeOffset] & 0xFF) << 24)
                      | ((wkb[typeOffset + 1] & 0xFF) << 16)
                      | ((wkb[typeOffset + 2] & 0xFF) << 8)
                      | (wkb[typeOffset + 3] & 0xFF);
            }

            // Handle SRID variations (add 0x20000000 mask)
            int baseType = wkbType & 0xFF;

            return (A)
                switch (baseType) {
                  case 1 -> rs.getObject(i, org.mariadb.jdbc.type.Point.class);
                  case 2 -> rs.getObject(i, org.mariadb.jdbc.type.LineString.class);
                  case 3 -> rs.getObject(i, org.mariadb.jdbc.type.Polygon.class);
                  case 4 -> rs.getObject(i, org.mariadb.jdbc.type.MultiPoint.class);
                  case 5 -> rs.getObject(i, org.mariadb.jdbc.type.MultiLineString.class);
                  case 6 -> rs.getObject(i, org.mariadb.jdbc.type.MultiPolygon.class);
                  case 7 -> rs.getObject(i, org.mariadb.jdbc.type.GeometryCollection.class);
                  default -> throw new SQLException("Unknown WKB geometry type: " + wkbType);
                };
          }

          throw new SQLException(
              "Expected " + cls.getName() + " but got: " + obj.getClass().getName());
        });
  }

  // Basic type readers
  MariaRead<String> readString = of(ResultSet::getString);
  MariaRead<Boolean> readBoolean = of(ResultSet::getBoolean);
  MariaRead<Byte> readByte = of(ResultSet::getByte);
  MariaRead<Short> readShort = of(ResultSet::getShort);
  MariaRead<Integer> readInteger = of(ResultSet::getInt);
  MariaRead<Long> readLong = of(ResultSet::getLong);
  MariaRead<Float> readFloat = of(ResultSet::getFloat);
  MariaRead<Double> readDouble = of(ResultSet::getDouble);
  MariaRead<BigDecimal> readBigDecimal = of(ResultSet::getBigDecimal);
  // For BINARY/VARBINARY - reads as byte[] directly
  MariaRead<byte[]> readByteArray = of(ResultSet::getBytes);

  // For BLOB types - MariaDB returns Blob objects, need to extract bytes
  MariaRead<byte[]> readBlob =
      of(
          (rs, idx) -> {
            java.sql.Blob blob = rs.getBlob(idx);
            if (blob == null) return null;
            return blob.getBytes(1, (int) blob.length());
          });

  // BigInteger for BIGINT UNSIGNED
  MariaRead<BigInteger> readBigInteger = readBigDecimal.map(bd -> bd.toBigInteger());

  // Date/Time readers
  MariaRead<LocalDate> readLocalDate = of((rs, idx) -> rs.getObject(idx, LocalDate.class));
  MariaRead<LocalTime> readLocalTime = of((rs, idx) -> rs.getObject(idx, LocalTime.class));
  MariaRead<LocalDateTime> readLocalDateTime =
      of((rs, idx) -> rs.getObject(idx, LocalDateTime.class));

  // Year type - MariaDB returns it as a short
  MariaRead<Year> readYear = readShort.map(s -> Year.of(s.intValue()));

  // BIT type - MariaDB returns as byte[] for BIT(n) where n > 1
  MariaRead<byte[]> readBit =
      of(
          (rs, idx) -> {
            Object obj = rs.getObject(idx);
            if (obj == null) return null;
            if (obj instanceof byte[]) return (byte[]) obj;
            if (obj instanceof Boolean) return new byte[] {(byte) (((Boolean) obj) ? 1 : 0)};
            if (obj instanceof Number) return new byte[] {((Number) obj).byteValue()};
            throw new SQLException("Cannot convert " + obj.getClass() + " to byte[] for BIT type");
          });

  // BIT(1) as Boolean
  MariaRead<Boolean> readBitAsBoolean =
      of(
          (rs, idx) -> {
            Object obj = rs.getObject(idx);
            if (obj == null) return null;
            if (obj instanceof Boolean) return (Boolean) obj;
            if (obj instanceof byte[]) {
              byte[] bytes = (byte[]) obj;
              return bytes.length > 0 && bytes[0] != 0;
            }
            if (obj instanceof Number) return ((Number) obj).intValue() != 0;
            throw new SQLException(
                "Cannot convert " + obj.getClass() + " to Boolean for BIT(1) type");
          });
}
