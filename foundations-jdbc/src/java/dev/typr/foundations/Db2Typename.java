package dev.typr.foundations;

import dev.typr.foundations.dsl.Bijection;
import java.util.Optional;

/**
 * Represents a DB2 SQL type name with optional precision. Similar to SqlServerTypename. DB2 uses
 * double quotes for identifiers and standard CAST syntax.
 */
public sealed interface Db2Typename<A> extends DbTypename<A> {
  String sqlType();

  /**
   * DB2 uses CAST() syntax, not PostgreSQL's :: operator. Don't render :: casts in prepared
   * statements.
   */
  @Override
  default boolean renderTypeCast() {
    return false;
  }

  String sqlTypeNoPrecision();

  Db2Typename<A> renamed(String value);

  Db2Typename<A> renamedDropPrecision(String value);

  default Db2Typename<Optional<A>> opt() {
    return new Opt<>(this);
  }

  default <B> Db2Typename<B> as() {
    return (Db2Typename<B>) this;
  }

  /**
   * Type-safe conversion using a bijection as proof of type relationship. Overrides DbTypename.to()
   * to return Db2Typename for better type refinement.
   */
  @Override
  default <B> Db2Typename<B> to(Bijection<A, B> bijection) {
    return (Db2Typename<B>) this;
  }

  record Base<A>(String sqlType) implements Db2Typename<A> {
    @Override
    public String sqlTypeNoPrecision() {
      return sqlType;
    }

    @Override
    public Base<A> renamed(String value) {
      return new Base<>(value);
    }

    @Override
    public Base<A> renamedDropPrecision(String value) {
      return new Base<>(value);
    }
  }

  record WithPrec<A>(Base<A> of, int precision) implements Db2Typename<A> {
    public String sqlType() {
      return of.sqlType + "(" + precision + ")";
    }

    @Override
    public String sqlTypeNoPrecision() {
      return of.sqlTypeNoPrecision();
    }

    @Override
    public Db2Typename<A> renamed(String value) {
      return new WithPrec<>(of.renamed(value), precision);
    }

    @Override
    public Db2Typename<A> renamedDropPrecision(String value) {
      return of.renamed(value);
    }
  }

  record WithPrecScale<A>(Base<A> of, int precision, int scale) implements Db2Typename<A> {
    public String sqlType() {
      return of.sqlType + "(" + precision + "," + scale + ")";
    }

    @Override
    public String sqlTypeNoPrecision() {
      return of.sqlTypeNoPrecision();
    }

    @Override
    public Db2Typename<A> renamed(String value) {
      return new WithPrecScale<>(of.renamed(value), precision, scale);
    }

    @Override
    public Db2Typename<A> renamedDropPrecision(String value) {
      return of.renamed(value);
    }
  }

  record Opt<A>(Db2Typename<A> of) implements Db2Typename<Optional<A>> {
    @Override
    public String sqlType() {
      return of.sqlType();
    }

    @Override
    public String sqlTypeNoPrecision() {
      return of.sqlTypeNoPrecision();
    }

    @Override
    public Db2Typename<Optional<A>> renamed(String value) {
      return new Opt<>(of.renamed(value));
    }

    @Override
    public Db2Typename<Optional<A>> renamedDropPrecision(String value) {
      return new Opt<>(of.renamedDropPrecision(value));
    }
  }

  static <T> Db2Typename<T> of(String sqlType) {
    return new Base<>(sqlType);
  }

  static <T> Db2Typename<T> of(String sqlType, int precision) {
    return new WithPrec<>(new Base<>(sqlType), precision);
  }

  static <T> Db2Typename<T> of(String sqlType, int precision, int scale) {
    return new WithPrecScale<>(new Base<>(sqlType), precision, scale);
  }
}
