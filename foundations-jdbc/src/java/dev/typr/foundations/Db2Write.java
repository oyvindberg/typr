package dev.typr.foundations;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.function.Function;

/**
 * Describes how to write a value to a {@link PreparedStatement} for IBM DB2.
 *
 * <p>Similar to SqlServerWrite but adapted for DB2-specific types like DECFLOAT, GRAPHIC, etc.
 */
public sealed interface Db2Write<A> extends DbWrite<A> permits Db2Write.Instance {
  void set(PreparedStatement ps, int idx, A a) throws SQLException;

  Db2Write<Optional<A>> opt(Db2Typename<A> typename);

  <B> Db2Write<B> contramap(Function<B, A> f);

  @FunctionalInterface
  interface RawWriter<A> {
    void set(PreparedStatement ps, int index, A a) throws SQLException;
  }

  record Instance<A, U>(RawWriter<U> rawWriter, Function<A, U> f) implements Db2Write<A> {
    @Override
    public void set(PreparedStatement ps, int index, A a) throws SQLException {
      rawWriter.set(ps, index, f.apply(a));
    }

    @Override
    public Db2Write<Optional<A>> opt(Db2Typename<A> typename) {
      int sqlType = getSqlTypeForTypename(typename.sqlTypeNoPrecision());
      return new Instance<>(
          (ps, index, u) -> {
            if (u == null) ps.setNull(index, sqlType);
            else set(ps, index, u);
          },
          a -> a.orElse(null));
    }

    @Override
    public <B> Db2Write<B> contramap(Function<B, A> f) {
      return new Instance<>(rawWriter, f.andThen(this.f));
    }
  }

  static <A> Db2Write<A> primitive(RawWriter<A> rawWriter) {
    return new Instance<>(rawWriter, Function.identity());
  }

  static <A> Db2Write<A> passObjectToJdbc() {
    return primitive(PreparedStatement::setObject);
  }

  static int getSqlTypeForTypename(String sqlType) {
    return switch (sqlType.toUpperCase()) {
      case "SMALLINT" -> java.sql.Types.SMALLINT;
      case "INTEGER", "INT" -> java.sql.Types.INTEGER;
      case "BIGINT" -> java.sql.Types.BIGINT;
      case "DECIMAL", "NUMERIC", "DEC" -> java.sql.Types.DECIMAL;
      case "DECFLOAT" -> java.sql.Types.DECIMAL;
      case "REAL" -> java.sql.Types.REAL;
      case "DOUBLE", "FLOAT" -> java.sql.Types.DOUBLE;
      case "BOOLEAN" -> java.sql.Types.BOOLEAN;
      case "CHAR", "CHARACTER" -> java.sql.Types.CHAR;
      case "VARCHAR" -> java.sql.Types.VARCHAR;
      case "CLOB" -> java.sql.Types.CLOB;
      case "GRAPHIC" -> java.sql.Types.CHAR;
      case "VARGRAPHIC" -> java.sql.Types.VARCHAR;
      case "DBCLOB" -> java.sql.Types.CLOB;
      case "BINARY" -> java.sql.Types.BINARY;
      case "VARBINARY" -> java.sql.Types.VARBINARY;
      case "BLOB" -> java.sql.Types.BLOB;
      case "DATE" -> java.sql.Types.DATE;
      case "TIME" -> java.sql.Types.TIME;
      case "TIMESTAMP" -> java.sql.Types.TIMESTAMP;
      case "XML" -> java.sql.Types.SQLXML;
      case "ROWID" -> java.sql.Types.ROWID;
      default -> java.sql.Types.OTHER;
    };
  }

  // ==================== Basic Type Writers ====================

  Db2Write<String> writeString = primitive(PreparedStatement::setString);
  Db2Write<Boolean> writeBoolean = primitive(PreparedStatement::setBoolean);
  Db2Write<Short> writeShort = primitive(PreparedStatement::setShort);
  Db2Write<Integer> writeInteger = primitive(PreparedStatement::setInt);
  Db2Write<Long> writeLong = primitive(PreparedStatement::setLong);
  Db2Write<Float> writeFloat = primitive(PreparedStatement::setFloat);
  Db2Write<Double> writeDouble = primitive(PreparedStatement::setDouble);
  Db2Write<BigDecimal> writeBigDecimal = primitive(PreparedStatement::setBigDecimal);
  Db2Write<byte[]> writeByteArray = primitive(PreparedStatement::setBytes);

  // ==================== Date/Time Writers ====================

  Db2Write<LocalDate> writeDate = primitive(PreparedStatement::setObject);
  Db2Write<LocalTime> writeTime = primitive(PreparedStatement::setObject);
  Db2Write<LocalDateTime> writeTimestamp = primitive(PreparedStatement::setObject);

  // ==================== Special Type Writers ====================

  // XML
  Db2Write<dev.typr.foundations.data.Xml> writeXml =
      primitive(
          (ps, idx, xml) -> {
            java.sql.SQLXML sqlxml = ps.getConnection().createSQLXML();
            sqlxml.setString(xml.value());
            ps.setSQLXML(idx, sqlxml);
          });

  // CLOB
  Db2Write<String> writeClob =
      primitive(
          (ps, idx, str) -> {
            java.sql.Clob clob = ps.getConnection().createClob();
            clob.setString(1, str);
            ps.setClob(idx, clob);
          });

  // BLOB
  Db2Write<byte[]> writeBlob =
      primitive(
          (ps, idx, bytes) -> {
            java.sql.Blob blob = ps.getConnection().createBlob();
            blob.setBytes(1, bytes);
            ps.setBlob(idx, blob);
          });

  // GRAPHIC types - written as strings
  Db2Write<String> writeGraphic = writeString;
  Db2Write<String> writeVarGraphic = writeString;
  Db2Write<String> writeDbClob = writeClob;

  // DECFLOAT - write as BigDecimal
  Db2Write<BigDecimal> writeDecFloat = writeBigDecimal;

  // ROWID
  Db2Write<byte[]> writeRowId = writeByteArray;

  // Generic object write
  Db2Write<Object> writeObject = primitive(PreparedStatement::setObject);
}
