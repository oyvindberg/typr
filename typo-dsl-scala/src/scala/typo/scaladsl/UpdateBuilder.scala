package typo.scaladsl

import typo.dsl.{SqlExpr, UpdateBuilder as JavaUpdateBuilder}
import typo.runtime.{Fragment, PgType}

import java.sql.Connection
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

class UpdateBuilder[Fields, Row] private[scaladsl] (
    private val javaBuilder: JavaUpdateBuilder[Fields, Row]
) {

  private def copy(newJavaBuilder: JavaUpdateBuilder[Fields, Row]): UpdateBuilder[Fields, Row] =
    new UpdateBuilder(newJavaBuilder)

  def set[T](field: Fields => SqlExpr.FieldLike[T, Row], value: T, pgType: PgType[T]): UpdateBuilder[Fields, Row] = {
    copy(javaBuilder.set(fields => field(fields), value, pgType))
  }

  def setValue[T](field: Fields => SqlExpr.FieldLike[T, Row], value: T): UpdateBuilder[Fields, Row] = {
    copy(javaBuilder.setValue(fields => field(fields), value))
  }

  def setExpr[T](field: Fields => SqlExpr.FieldLike[T, Row], expr: SqlExpr[T]): UpdateBuilder[Fields, Row] = {
    copy(javaBuilder.setExpr(fields => field(fields), expr))
  }

  def setComputedValue[T](
      field: Fields => SqlExpr.FieldLike[T, Row],
      compute: SqlExpr.FieldLike[T, Row] => SqlExpr[T]
  ): UpdateBuilder[Fields, Row] = {
    copy(javaBuilder.setComputedValue(fields => field(fields), fieldLike => compute(fieldLike)))
  }

  def where(predicate: Fields => SqlExpr[java.lang.Boolean]): UpdateBuilder[Fields, Row] = {
    copy(javaBuilder.where(fields => predicate(fields)))
  }

  def execute(connection: Connection): Int = {
    javaBuilder.execute(connection)
  }

  def executeReturning(connection: Connection): List[Row] = {
    javaBuilder.executeReturning(connection).asScala.toList
  }

  def sql(): Option[Fragment] = {
    javaBuilder.sql().toScala
  }
}

object UpdateBuilder {
  def apply[Fields, Row](javaBuilder: JavaUpdateBuilder[Fields, Row]): UpdateBuilder[Fields, Row] =
    new UpdateBuilder(javaBuilder)

  def of[Fields, Row](
      tableName: String,
      structure: RelationStructure[Fields, Row],
      rowParser: RowParser[Row],
      dialect: typo.dsl.Dialect
  ): UpdateBuilder[Fields, Row] = {
    new UpdateBuilder(JavaUpdateBuilder.of(tableName, structure, rowParser.underlying, dialect))
  }
}
