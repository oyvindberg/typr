package dev.typr.foundations;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

/**
 * Describes how to read a column from a {@link ResultSet} for IBM DB2.
 *
 * <p>Similar to SqlServerRead but adapted for DB2-specific types.
 */
public sealed interface Db2Read<A> extends DbRead<A>
    permits Db2Read.NonNullable, Db2Read.Nullable, Db2Read.Mapped {
  A read(ResultSet rs, int col) throws SQLException;

  <B> Db2Read<B> map(SqlFunction<A, B> f);

  /** Derive a Db2Read which allows nullable values */
  Db2Read<Optional<A>> opt();

  @FunctionalInterface
  interface RawRead<A> {
    A apply(ResultSet rs, int column) throws SQLException;
  }

  /**
   * Create an instance of {@link Db2Read} from a function that reads a value from a result set.
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

  final class NonNullable<A> implements Db2Read<A> {
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
    public Db2Read<Optional<A>> opt() {
      return new Nullable<>(readNullable);
    }
  }

  final class Nullable<A> implements Db2Read<Optional<A>> {
    final RawRead<Optional<A>> readNullable;

    public Nullable(RawRead<Optional<A>> readNullable) {
      this.readNullable = readNullable;
    }

    @Override
    public Optional<A> read(ResultSet rs, int col) throws SQLException {
      return readNullable.apply(rs, col);
    }

    @Override
    public <B> Db2Read<B> map(SqlFunction<Optional<A>, B> f) {
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

  record Mapped<A, B>(Db2Read<A> underlying, SqlFunction<A, B> f) implements Db2Read<B> {
    @Override
    public B read(ResultSet rs, int col) throws SQLException {
      return f.apply(underlying.read(rs, col));
    }

    @Override
    public <C> Db2Read<C> map(SqlFunction<B, C> g) {
      return new Mapped<>(this, g);
    }

    @Override
    public Db2Read<Optional<B>> opt() {
      return new Nullable<>((rs, col) -> Optional.ofNullable(read(rs, col)));
    }
  }

  static <A> NonNullable<A> castJdbcObjectTo(Class<A> cls) {
    return of((rs, i) -> cls.cast(rs.getObject(i)));
  }

  /**
   * Read a value by requesting a specific class from JDBC. This uses rs.getObject(i, cls) which
   * allows the JDBC driver to do proper type conversion.
   */
  static <A> NonNullable<A> getObjectAs(Class<A> cls) {
    return of((rs, i) -> rs.getObject(i, cls));
  }

  // ==================== Basic Type Readers ====================

  Db2Read<String> readString = of(ResultSet::getString);
  Db2Read<Boolean> readBoolean = of(ResultSet::getBoolean);
  Db2Read<Short> readShort = of(ResultSet::getShort);
  Db2Read<Integer> readInteger = of(ResultSet::getInt);
  Db2Read<Long> readLong = of(ResultSet::getLong);
  Db2Read<Float> readFloat = of(ResultSet::getFloat);
  Db2Read<Double> readDouble = of(ResultSet::getDouble);
  Db2Read<BigDecimal> readBigDecimal = of(ResultSet::getBigDecimal);

  // Binary types
  Db2Read<byte[]> readByteArray = of(ResultSet::getBytes);
  Db2Read<byte[]> readBlob =
      of(
          (rs, idx) -> {
            java.sql.Blob blob = rs.getBlob(idx);
            if (blob == null) return null;
            return blob.getBytes(1, (int) blob.length());
          });

  // ==================== Date/Time Readers ====================

  // DB2 JDBC driver doesn't handle null properly in getObject(idx, LocalDate.class)
  // so we use the traditional approach with explicit null checking
  Db2Read<LocalDate> readDate =
      of(
          (rs, idx) -> {
            java.sql.Date date = rs.getDate(idx);
            return date == null ? null : date.toLocalDate();
          });
  Db2Read<LocalTime> readTime =
      of(
          (rs, idx) -> {
            java.sql.Time time = rs.getTime(idx);
            return time == null ? null : time.toLocalTime();
          });
  Db2Read<LocalDateTime> readTimestamp =
      of(
          (rs, idx) -> {
            java.sql.Timestamp timestamp = rs.getTimestamp(idx);
            return timestamp == null ? null : timestamp.toLocalDateTime();
          });

  // ==================== Special Types ====================

  // XML - DB2 supports XML natively
  Db2Read<dev.typr.foundations.data.Xml> readXml =
      of(
          (rs, idx) -> {
            java.sql.SQLXML sqlxml = rs.getSQLXML(idx);
            if (sqlxml == null) return null;
            return new dev.typr.foundations.data.Xml(sqlxml.getString());
          });

  // CLOB - Character Large Object
  Db2Read<String> readClob =
      of(
          (rs, idx) -> {
            java.sql.Clob clob = rs.getClob(idx);
            if (clob == null) return null;
            return clob.getSubString(1, (int) clob.length());
          });

  // GRAPHIC types - DBCS (double-byte character sets)
  // These are read as strings in Java
  Db2Read<String> readGraphic = readString;
  Db2Read<String> readVarGraphic = readString;
  Db2Read<String> readDbClob = readClob;

  // ROWID - DB2 row identifier
  Db2Read<byte[]> readRowId = readByteArray;

  // DECFLOAT - DB2-specific decimal floating point
  Db2Read<BigDecimal> readDecFloat = readBigDecimal;

  // Read as Object for unknown types
  Db2Read<Object> readObject = of(ResultSet::getObject);
}
