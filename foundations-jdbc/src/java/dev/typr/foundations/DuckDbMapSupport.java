package dev.typr.foundations;

import java.util.function.Function;

/**
 * Handles conversion of values to/from DuckDB MAP entries. DuckDB JDBC returns maps with Object
 * keys/values that need to be cast to the proper types, and when writing we may need to convert
 * back.
 *
 * @param <A> the Java type
 */
public interface DuckDbMapSupport<A> {
  /** Convert a raw value from a DuckDB MAP to the Java type. */
  A fromMap(Object raw);

  /** Convert the Java type to a value suitable for a DuckDB MAP. Usually identity. */
  Object toMap(A value);

  /** Create a support that just casts (for types DuckDB returns directly). */
  @SuppressWarnings("unchecked")
  static <A> DuckDbMapSupport<A> cast() {
    return new DuckDbMapSupport<>() {
      @Override
      public A fromMap(Object raw) {
        return (A) raw;
      }

      @Override
      public Object toMap(A value) {
        return value;
      }
    };
  }

  /** Create a support with custom conversion in both directions. */
  static <A> DuckDbMapSupport<A> of(Function<Object, A> from, Function<A, Object> to) {
    return new DuckDbMapSupport<>() {
      @Override
      public A fromMap(Object raw) {
        return from.apply(raw);
      }

      @Override
      public Object toMap(A value) {
        return to.apply(value);
      }
    };
  }

  /** Create a support with custom read conversion, identity for write. */
  static <A> DuckDbMapSupport<A> fromOnly(Function<Object, A> from) {
    return new DuckDbMapSupport<>() {
      @Override
      public A fromMap(Object raw) {
        return from.apply(raw);
      }

      @Override
      public Object toMap(A value) {
        return value;
      }
    };
  }

  /** Transform this support with a bijection (for bimap support). */
  default <B> DuckDbMapSupport<B> bimap(Function<A, B> f, Function<B, A> g) {
    DuckDbMapSupport<A> self = this;
    return new DuckDbMapSupport<>() {
      @Override
      public B fromMap(Object raw) {
        return f.apply(self.fromMap(raw));
      }

      @Override
      public Object toMap(B value) {
        return self.toMap(g.apply(value));
      }
    };
  }
}
