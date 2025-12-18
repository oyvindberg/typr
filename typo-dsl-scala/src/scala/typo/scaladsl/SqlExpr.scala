package typo.scaladsl

import typo.runtime.DbType

import java.util.Optional
import java.util.concurrent.atomic.AtomicInteger
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

object SqlExpr {

  /** Bijection between java.lang.Boolean and scala.Boolean (identity at runtime) */
  private val JavaToScalaBool: typo.dsl.Bijection[java.lang.Boolean, Boolean] =
    typo.dsl.Bijection.of[java.lang.Boolean, Boolean](
      (jb: java.lang.Boolean) => jb: Boolean,
      (sb: Boolean) => sb: java.lang.Boolean
    )

  /** Convert java.lang.Boolean SqlExpr to scala.Boolean SqlExpr using underlying method */
  def toScalaBool(expr: typo.dsl.SqlExpr[java.lang.Boolean]): typo.dsl.SqlExpr[Boolean] =
    expr.underlying(JavaToScalaBool)

  /** Bijection between scala.Boolean and java.lang.Boolean (identity at runtime) */
  private val ScalaToJavaBool: typo.dsl.Bijection[Boolean, java.lang.Boolean] =
    typo.dsl.Bijection.of[Boolean, java.lang.Boolean](
      (sb: Boolean) => sb: java.lang.Boolean,
      (jb: java.lang.Boolean) => jb: Boolean
    )

  /** Convert scala.Boolean SqlExpr to java.lang.Boolean SqlExpr using underlying method */
  def toJavaBool(expr: typo.dsl.SqlExpr[Boolean]): typo.dsl.SqlExpr[java.lang.Boolean] =
    expr.underlying(ScalaToJavaBool)

  /** Wrapper for SqlExpr.all that accepts and returns scala.Boolean */
  def all(exprs: typo.dsl.SqlExpr[Boolean]*): typo.dsl.SqlExpr[Boolean] =
    toScalaBool(typo.dsl.SqlExpr.all(exprs.map(toJavaBool)*))

  trait FieldLike[T, Row] {
    val underlying: typo.dsl.SqlExpr.FieldLike[T, Row]

    def path(): List[typo.dsl.Path] = underlying._path().asScala.toList
    def column(): String = underlying.column()
    def get(row: Row): Optional[T] = underlying.get(row)
    def set(row: Row, value: Optional[T]): typo.runtime.Either[String, Row] = underlying.set(row, value)
    def sqlReadCast(): Optional[String] = underlying.sqlReadCast()
    def sqlWriteCast(): Optional[String] = underlying.sqlWriteCast()
    def pgType(): typo.runtime.DbType[T] = underlying.pgType()
    def render(ctx: typo.dsl.RenderCtx, counter: AtomicInteger): typo.runtime.Fragment =
      underlying.render(ctx, counter)

    // Comparison operators - all return scala.Boolean
    def isEqual(other: typo.dsl.SqlExpr[T]): typo.dsl.SqlExpr[Boolean] =
      toScalaBool(underlying.isEqual(other))

    def isEqual(value: T): typo.dsl.SqlExpr[Boolean] =
      toScalaBool(underlying.isEqual(value))

    def isNotEqual(other: typo.dsl.SqlExpr[T]): typo.dsl.SqlExpr[Boolean] =
      toScalaBool(underlying.isNotEqual(other))

    def isNotEqual(value: T): typo.dsl.SqlExpr[Boolean] =
      toScalaBool(underlying.isNotEqual(value))

    def greaterThan(other: typo.dsl.SqlExpr[T]): typo.dsl.SqlExpr[Boolean] =
      toScalaBool(underlying.greaterThan(other))

    def greaterThan(value: T): typo.dsl.SqlExpr[Boolean] =
      toScalaBool(underlying.greaterThan(value))

    def greaterThanOrEqual(other: typo.dsl.SqlExpr[T]): typo.dsl.SqlExpr[Boolean] =
      toScalaBool(underlying.greaterThanOrEqual(other))

    def greaterThanOrEqual(value: T): typo.dsl.SqlExpr[Boolean] =
      toScalaBool(underlying.greaterThanOrEqual(value))

    def lessThan(other: typo.dsl.SqlExpr[T]): typo.dsl.SqlExpr[Boolean] =
      toScalaBool(underlying.lessThan(other))

    def lessThan(value: T): typo.dsl.SqlExpr[Boolean] =
      toScalaBool(underlying.lessThan(value))

    def lessThanOrEqual(other: typo.dsl.SqlExpr[T]): typo.dsl.SqlExpr[Boolean] =
      toScalaBool(underlying.lessThanOrEqual(other))

    def lessThanOrEqual(value: T): typo.dsl.SqlExpr[Boolean] =
      toScalaBool(underlying.lessThanOrEqual(value))

    // Logical operators
    def or(other: typo.dsl.SqlExpr[T], bijection: typo.dsl.Bijection[T, java.lang.Boolean]): typo.dsl.SqlExpr[T] =
      underlying.or(other, bijection)

    def and(other: typo.dsl.SqlExpr[T], bijection: typo.dsl.Bijection[T, java.lang.Boolean]): typo.dsl.SqlExpr[T] =
      underlying.and(other, bijection)

    def not(bijection: typo.dsl.Bijection[T, java.lang.Boolean]): typo.dsl.SqlExpr[T] =
      underlying.not(bijection)

    // Arithmetic operators
    def plus(other: typo.dsl.SqlExpr[T]): typo.dsl.SqlExpr[T] =
      underlying.plus(other)

    def minus(other: typo.dsl.SqlExpr[T]): typo.dsl.SqlExpr[T] =
      underlying.minus(other)

    def multiply(other: typo.dsl.SqlExpr[T]): typo.dsl.SqlExpr[T] =
      underlying.multiply(other)

    // String operations
    def like(pattern: String, bijection: typo.dsl.Bijection[T, String]): typo.dsl.SqlExpr[Boolean] =
      toScalaBool(underlying.like(pattern, bijection))

    def stringAppend(other: typo.dsl.SqlExpr[T], bijection: typo.dsl.Bijection[T, String]): typo.dsl.SqlExpr[T] =
      underlying.stringAppend(other, bijection)

    def lower(bijection: typo.dsl.Bijection[T, String]): typo.dsl.SqlExpr[T] =
      underlying.lower(bijection)

    def upper(bijection: typo.dsl.Bijection[T, String]): typo.dsl.SqlExpr[T] =
      underlying.upper(bijection)

    def reverse(bijection: typo.dsl.Bijection[T, String]): typo.dsl.SqlExpr[T] =
      underlying.reverse(bijection)

    def strpos(substring: typo.dsl.SqlExpr[String], bijection: typo.dsl.Bijection[T, String]): typo.dsl.SqlExpr[java.lang.Integer] =
      underlying.strpos(substring, bijection)

    def strLength(bijection: typo.dsl.Bijection[T, String]): typo.dsl.SqlExpr[java.lang.Integer] =
      underlying.strLength(bijection)

    def substring(from: typo.dsl.SqlExpr[java.lang.Integer], count: typo.dsl.SqlExpr[java.lang.Integer], bijection: typo.dsl.Bijection[T, String]): typo.dsl.SqlExpr[T] =
      underlying.substring(from, count, bijection)

    // Null handling
    def isNull(): typo.dsl.SqlExpr[Boolean] =
      toScalaBool(underlying.isNull())

    def coalesce(defaultValue: typo.dsl.SqlExpr[T]): typo.dsl.SqlExpr[T] =
      underlying.coalesce(defaultValue)

    // Type conversion
    def underlyingValue[TT](bijection: typo.dsl.Bijection[T, TT]): typo.dsl.SqlExpr[TT] =
      underlying.underlying(bijection)

    // Array operations
    def in(values: Array[Object], pgType: typo.runtime.DbType[T]): typo.dsl.SqlExpr[Boolean] =
      toScalaBool(underlying.in(values.asInstanceOf[Array[Object & T]], pgType))

    // Custom operators
    def customBinaryOp[T2](op: String, right: typo.dsl.SqlExpr[T2], eval: (T, T2) => Boolean): typo.dsl.SqlExpr[Boolean] =
      toScalaBool(underlying.customBinaryOp(op, right, (a: T, b: T2) => java.lang.Boolean.valueOf(eval(a, b))))

    // Sorting
    def asc: typo.dsl.SortOrder[T] = typo.dsl.SortOrder.asc(underlying)
    def desc: typo.dsl.SortOrder[T] = typo.dsl.SortOrder.desc(underlying)
  }

  case class Field[T, Row](override val underlying: typo.dsl.SqlExpr.Field[T, Row]) extends FieldLike[T, Row] {
    def this(
        path: List[typo.dsl.Path],
        column: String,
        get: Row => T,
        sqlReadCast: Option[String],
        sqlWriteCast: Option[String],
        setter: (Row, T) => Row,
        pgType: DbType[T]
    ) = this(
      typo.dsl.SqlExpr.Field[T, Row](
        path.asJava,
        column,
        (row: Row) => get(row),
        sqlReadCast.toJava,
        sqlWriteCast.toJava,
        (row: Row, value: T) => setter(row, value),
        pgType
      )
    )

    // Constructor accepting Java List for interface compatibility
    def this(
        path: java.util.List[typo.dsl.Path],
        column: String,
        get: Row => T,
        sqlReadCast: Option[String],
        sqlWriteCast: Option[String],
        setter: (Row, T) => Row,
        pgType: DbType[T]
    ) = this(path.asScala.toList, column, get, sqlReadCast, sqlWriteCast, setter, pgType)
  }

  case class OptField[T, Row](override val underlying: typo.dsl.SqlExpr.OptField[T, Row]) extends FieldLike[T, Row] {
    def this(
        path: List[typo.dsl.Path],
        column: String,
        get: Row => Option[T],
        sqlReadCast: Option[String],
        sqlWriteCast: Option[String],
        setter: (Row, Option[T]) => Row,
        pgType: DbType[T]
    ) = this(
      typo.dsl.SqlExpr.OptField[T, Row](
        path.asJava,
        column,
        (row: Row) => get(row).toJava,
        sqlReadCast.toJava,
        sqlWriteCast.toJava,
        (row: Row, value: Optional[T]) => setter(row, value.toScala),
        pgType
      )
    )

    // Constructor accepting Java List for interface compatibility
    def this(
        path: java.util.List[typo.dsl.Path],
        column: String,
        get: Row => Option[T],
        sqlReadCast: Option[String],
        sqlWriteCast: Option[String],
        setter: (Row, Option[T]) => Row,
        pgType: DbType[T]
    ) = this(path.asScala.toList, column, get, sqlReadCast, sqlWriteCast, setter, pgType)

    def getOrNone(row: Row): Option[T] = underlying.get(row).toScala
  }

  case class IdField[T, Row](override val underlying: typo.dsl.SqlExpr.IdField[T, Row]) extends FieldLike[T, Row] {
    def this(
        path: List[typo.dsl.Path],
        column: String,
        get: Row => T,
        sqlReadCast: Option[String],
        sqlWriteCast: Option[String],
        setter: (Row, T) => Row,
        pgType: DbType[T]
    ) = this(
      typo.dsl.SqlExpr.IdField[T, Row](
        path.asJava,
        column,
        (row: Row) => get(row),
        sqlReadCast.toJava,
        sqlWriteCast.toJava,
        (row: Row, value: T) => setter(row, value),
        pgType
      )
    )

    // Constructor accepting Java List for interface compatibility
    def this(
        path: java.util.List[typo.dsl.Path],
        column: String,
        get: Row => T,
        sqlReadCast: Option[String],
        sqlWriteCast: Option[String],
        setter: (Row, T) => Row,
        pgType: DbType[T]
    ) = this(path.asScala.toList, column, get, sqlReadCast, sqlWriteCast, setter, pgType)
  }

  object CompositeIn {
    def apply[Tuple, Row](
        parts: List[typo.dsl.SqlExpr.CompositeIn.Part[?, Tuple, Row]],
        tuples: List[Tuple]
    ): typo.dsl.SqlExpr[Boolean] = {
      toScalaBool(typo.dsl.SqlExpr.CompositeIn(parts.asJava, tuples.asJava))
    }

    def Part[Id, Tuple, Row](
        field: FieldLike[Id, Row],
        extract: Tuple => Id,
        pgType: typo.runtime.DbType[Id]
    ): typo.dsl.SqlExpr.CompositeIn.Part[Id, Tuple, Row] = {
      typo.dsl.SqlExpr.CompositeIn.Part(
        field.underlying,
        (tuple: Tuple) => extract(tuple),
        pgType
      )
    }
  }
}

class ForeignKey[Fields, Row](val underlying: typo.dsl.ForeignKey[Fields, Row]) {
  def withColumnPair[T](
      thisField: SqlExpr.FieldLike[T, ?],
      otherGetter: Fields => SqlExpr.FieldLike[T, Row]
  ): ForeignKey[Fields, Row] = {
    new ForeignKey(
      underlying.withColumnPair(
        thisField.underlying,
        (fields: Fields) => otherGetter(fields).underlying
      )
    )
  }
}

object ForeignKey {
  def of[Fields, Row](constraintName: String): ForeignKey[Fields, Row] = {
    new ForeignKey(typo.dsl.ForeignKey.of(constraintName))
  }
}
