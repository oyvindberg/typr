package dev.typr.foundations;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Encodes values to text format for DB2 LOAD command.
 *
 * <p>Similar to SqlServerText but adapted for DB2's text format.
 */
public abstract class Db2Text<A> implements DbText<A> {
  public abstract void unsafeEncode(A a, StringBuilder sb);

  public <B> Db2Text<B> contramap(Function<B, A> f) {
    var self = this;
    return instance((b, sb) -> self.unsafeEncode(f.apply(b), sb));
  }

  public Db2Text<Optional<A>> opt() {
    var self = this;
    return instance(
        (a, sb) -> {
          if (a.isPresent()) self.unsafeEncode(a.get(), sb);
          else sb.append(Db2Text.NULL);
        });
  }

  public static char DELIMETER = '\t';
  public static String NULL = "\\N";

  public static <A> Db2Text<A> instance(BiConsumer<A, StringBuilder> f) {
    return new Db2Text<>() {
      @Override
      public void unsafeEncode(A a, StringBuilder sb) {
        f.accept(a, sb);
      }
    };
  }

  @SuppressWarnings("unchecked")
  public static <A> Db2Text<A> from(RowParser<A> rowParser) {
    return instance(
        (row, sb) -> {
          var encoded = rowParser.encode().apply(row);
          for (int i = 0; i < encoded.length; i++) {
            if (i > 0) {
              sb.append(Db2Text.DELIMETER);
            }
            DbText<Object> text = (DbText<Object>) rowParser.columns().get(i).text();
            text.unsafeEncode(encoded[i], sb);
          }
        });
  }

  public static <A> Db2Text<A> instanceToString() {
    return textString.contramap(Object::toString);
  }

  /** DB2 doesn't support streaming like PostgreSQL's COPY, so this is a placeholder. */
  public static <A> Db2Text<A> NotWorking() {
    return instance(
        (a, sb) -> {
          throw new UnsupportedOperationException("DB2 text encoding not supported for this type");
        });
  }

  private static void escapeString(String s, StringBuilder sb) {
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '\0':
          sb.append("\\0");
          break;
        case '\n':
          sb.append("\\n");
          break;
        case '\r':
          sb.append("\\r");
          break;
        case '\t':
          sb.append("\\t");
          break;
        case '\\':
          sb.append("\\\\");
          break;
        default:
          sb.append(c);
      }
    }
  }

  // Basic type text encoders
  public static final Db2Text<String> textString = instance((s, sb) -> escapeString(s, sb));
  public static final Db2Text<Boolean> textBoolean = instanceToString();
  public static final Db2Text<Short> textShort = instanceToString();
  public static final Db2Text<Integer> textInteger = instanceToString();
  public static final Db2Text<Long> textLong = instanceToString();
  public static final Db2Text<Float> textFloat = instanceToString();
  public static final Db2Text<Double> textDouble = instanceToString();
  public static final Db2Text<BigDecimal> textBigDecimal = instanceToString();
  public static final Db2Text<byte[]> textByteArray =
      instance((bytes, sb) -> sb.append(java.util.Base64.getEncoder().encodeToString(bytes)));
  public static final Db2Text<Object> textObject = instanceToString();
}
