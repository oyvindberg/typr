package dev.typr.foundations;

import dev.typr.foundations.dsl.Bijection;
import java.util.Optional;
import java.util.function.Function;

/**
 * Combines DB2 type name, read, write, text encoding, and JSON encoding for a type. Similar to
 * SqlServerType but for IBM DB2.
 */
public record Db2Type<A>(
    Db2Typename<A> typename,
    Db2Read<A> read,
    Db2Write<A> write,
    Db2Text<A> db2Text,
    Db2Json<A> db2Json)
    implements DbType<A> {
  @Override
  public DbText<A> text() {
    return db2Text;
  }

  @Override
  public DbJson<A> json() {
    return db2Json;
  }

  public Db2Type<A> withTypename(Db2Typename<A> typename) {
    return new Db2Type<>(typename, read, write, db2Text, db2Json);
  }

  public Db2Type<A> withTypename(String sqlType) {
    return withTypename(Db2Typename.of(sqlType));
  }

  public Db2Type<A> renamed(String value) {
    return withTypename(typename.renamed(value));
  }

  public Db2Type<A> renamedDropPrecision(String value) {
    return withTypename(typename.renamedDropPrecision(value));
  }

  public Db2Type<A> withRead(Db2Read<A> read) {
    return new Db2Type<>(typename, read, write, db2Text, db2Json);
  }

  public Db2Type<A> withWrite(Db2Write<A> write) {
    return new Db2Type<>(typename, read, write, db2Text, db2Json);
  }

  public Db2Type<A> withText(Db2Text<A> text) {
    return new Db2Type<>(typename, read, write, text, db2Json);
  }

  public Db2Type<A> withJson(Db2Json<A> json) {
    return new Db2Type<>(typename, read, write, db2Text, json);
  }

  public Db2Type<Optional<A>> opt() {
    return new Db2Type<>(
        typename.opt(), read.opt(), write.opt(typename), db2Text.opt(), db2Json.opt());
  }

  public <B> Db2Type<B> bimap(SqlFunction<A, B> f, Function<B, A> g) {
    return new Db2Type<>(
        typename.as(), read.map(f), write.contramap(g), db2Text.contramap(g), db2Json.bimap(f, g));
  }

  public <B> Db2Type<B> to(Bijection<A, B> bijection) {
    return new Db2Type<>(
        typename.as(),
        read.map(bijection::underlying),
        write.contramap(bijection::from),
        db2Text.contramap(bijection::from),
        db2Json.bimap(bijection::underlying, bijection::from));
  }

  public static <A> Db2Type<A> of(
      String tpe, Db2Read<A> r, Db2Write<A> w, Db2Text<A> t, Db2Json<A> j) {
    return new Db2Type<>(Db2Typename.of(tpe), r, w, t, j);
  }

  public static <A> Db2Type<A> of(
      Db2Typename<A> typename, Db2Read<A> r, Db2Write<A> w, Db2Text<A> t, Db2Json<A> j) {
    return new Db2Type<>(typename, r, w, t, j);
  }
}
