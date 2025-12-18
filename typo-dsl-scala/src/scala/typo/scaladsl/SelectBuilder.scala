package typo.scaladsl

import typo.dsl.Tuple2
import typo.runtime.Fragment

import java.sql.Connection
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

class SelectBuilder[Fields, Row](private[scaladsl] val javaBuilder: typo.dsl.SelectBuilder[Fields, Row]) {

  def renderCtx(): typo.dsl.RenderCtx = javaBuilder.renderCtx()

  def structure(): typo.dsl.Structure[Fields, Row] = javaBuilder.structure()

  def where(predicate: Fields => typo.dsl.SqlExpr[Boolean]): SelectBuilder[Fields, Row] = {
    new SelectBuilder(javaBuilder.where(fields => predicate(fields).underlying(Bijections.scalaBooleanToJavaBoolean)))
  }

  def maybeWhere[T](value: Option[T], predicate: (Fields, T) => typo.dsl.SqlExpr[java.lang.Boolean]): SelectBuilder[Fields, Row] = {
    new SelectBuilder(javaBuilder.maybeWhere(value.toJava, (fields: Fields, t: T) => predicate(fields, t)))
  }

  def orderBy[T](orderFunc: Fields => typo.dsl.SortOrder[T]): SelectBuilder[Fields, Row] = {
    new SelectBuilder(javaBuilder.orderBy(fields => orderFunc(fields)))
  }

  def seek[T](orderFunc: Fields => typo.dsl.SortOrder[T], value: typo.dsl.SqlExpr.Const[T]): SelectBuilder[Fields, Row] = {
    new SelectBuilder(javaBuilder.seek(fields => orderFunc(fields), value))
  }

  def maybeSeek[T](
      orderFunc: Fields => typo.dsl.SortOrder[T],
      maybeValue: Option[T],
      asConst: T => typo.dsl.SqlExpr.Const[T]
  ): SelectBuilder[Fields, Row] = {
    new SelectBuilder(javaBuilder.maybeSeek(fields => orderFunc(fields), maybeValue.toJava, t => asConst(t)))
  }

  def offset(offset: Int): SelectBuilder[Fields, Row] = {
    new SelectBuilder(javaBuilder.offset(offset))
  }

  def limit(limit: Int): SelectBuilder[Fields, Row] = {
    new SelectBuilder(javaBuilder.limit(limit))
  }

  def toList(implicit connection: Connection): List[Row] = {
    javaBuilder.toList(connection).asScala.toList
  }

  def count(implicit connection: Connection): Int = {
    javaBuilder.count(connection)
  }

  def sql(): Option[Fragment] = {
    javaBuilder.sql().toScala
  }

  def joinFk[Fields2, Row2](
      fkFunc: Fields => typo.dsl.ForeignKey[Fields2, Row2],
      other: SelectBuilder[Fields2, Row2]
  ): SelectBuilder[Tuple2[Fields, Fields2], Tuple2[Row, Row2]] = {
    new SelectBuilder(javaBuilder.joinFk(fields => fkFunc(fields), other.javaBuilder))
  }

  def joinFk[Fields2, Row2](
      fkFunc: Fields => ForeignKey[Fields2, Row2],
      other: SelectBuilder[Fields2, Row2]
  )(using DummyImplicit): SelectBuilder[Tuple2[Fields, Fields2], Tuple2[Row, Row2]] = {
    new SelectBuilder(javaBuilder.joinFk(fields => fkFunc(fields).underlying, other.javaBuilder))
  }

  def join[Fields2, Row2](other: SelectBuilder[Fields2, Row2]): PartialJoin[Fields, Row, Fields2, Row2] = {
    new PartialJoin(this, other)
  }

  def joinOn[Fields2, Row2](
      other: SelectBuilder[Fields2, Row2],
      pred: Tuple2[Fields, Fields2] => typo.dsl.SqlExpr[java.lang.Boolean]
  ): SelectBuilder[Tuple2[Fields, Fields2], Tuple2[Row, Row2]] = {
    new SelectBuilder(javaBuilder.joinOn(other.javaBuilder, tuple => pred(tuple)))
  }

  def leftJoinOn[Fields2, Row2](
      other: SelectBuilder[Fields2, Row2],
      pred: Tuple2[Fields, Fields2] => typo.dsl.SqlExpr[java.lang.Boolean]
  ): LeftJoinSelectBuilder[Fields, Fields2, Row, Row2] = {
    val javaResult: typo.dsl.SelectBuilder[Tuple2[Fields, Fields2], Tuple2[Row, java.util.Optional[Row2]]] =
      javaBuilder.leftJoinOn(other.javaBuilder, tuple => pred(tuple))
    new LeftJoinSelectBuilder(javaResult)
  }

  def multisetOn[Fields2, Row2](
      other: SelectBuilder[Fields2, Row2],
      pred: Tuple2[Fields, Fields2] => typo.dsl.SqlExpr[java.lang.Boolean]
  ): MultisetSelectBuilder[Fields, Fields2, Row, Row2] = {
    val javaResult: typo.dsl.SelectBuilder[Tuple2[Fields, Fields2], Tuple2[Row, java.util.List[Row2]]] =
      javaBuilder.multisetOn(other.javaBuilder, tuple => pred(tuple))
    new MultisetSelectBuilder(javaResult)
  }

  def groupBy[G](groupKey: Fields => typo.dsl.SqlExpr[G]): typo.dsl.GroupedBuilder[Fields, Row] = {
    javaBuilder.groupBy(fields => groupKey(fields))
  }

  def groupBy[G1, G2](
      key1: Fields => typo.dsl.SqlExpr[G1],
      key2: Fields => typo.dsl.SqlExpr[G2]
  ): typo.dsl.GroupedBuilder[Fields, Row] = {
    javaBuilder.groupBy(fields => key1(fields), fields => key2(fields))
  }

  def groupBy[G1, G2, G3](
      key1: Fields => typo.dsl.SqlExpr[G1],
      key2: Fields => typo.dsl.SqlExpr[G2],
      key3: Fields => typo.dsl.SqlExpr[G3]
  ): typo.dsl.GroupedBuilder[Fields, Row] = {
    javaBuilder.groupBy(fields => key1(fields), fields => key2(fields), fields => key3(fields))
  }

  def groupBy[G1, G2, G3, G4](
      key1: Fields => typo.dsl.SqlExpr[G1],
      key2: Fields => typo.dsl.SqlExpr[G2],
      key3: Fields => typo.dsl.SqlExpr[G3],
      key4: Fields => typo.dsl.SqlExpr[G4]
  ): typo.dsl.GroupedBuilder[Fields, Row] = {
    javaBuilder.groupBy(fields => key1(fields), fields => key2(fields), fields => key3(fields), fields => key4(fields))
  }

  def groupBy[G1, G2, G3, G4, G5](
      key1: Fields => typo.dsl.SqlExpr[G1],
      key2: Fields => typo.dsl.SqlExpr[G2],
      key3: Fields => typo.dsl.SqlExpr[G3],
      key4: Fields => typo.dsl.SqlExpr[G4],
      key5: Fields => typo.dsl.SqlExpr[G5]
  ): typo.dsl.GroupedBuilder[Fields, Row] = {
    javaBuilder.groupBy(fields => key1(fields), fields => key2(fields), fields => key3(fields), fields => key4(fields), fields => key5(fields))
  }

  def groupByExpr(groupKeys: Fields => List[typo.dsl.SqlExpr[?]]): typo.dsl.GroupedBuilder[Fields, Row] = {
    javaBuilder.groupByExpr(fields => groupKeys(fields).asJava)
  }

  class PartialJoin[Fields, Row, Fields2, Row2](
      private val parent: SelectBuilder[Fields, Row],
      private val other: SelectBuilder[Fields2, Row2]
  ) {
    def onFk(fkFunc: Fields => typo.dsl.ForeignKey[Fields2, Row2]): SelectBuilder[Tuple2[Fields, Fields2], Tuple2[Row, Row2]] = {
      parent.joinFk(fields => fkFunc(fields), other)
    }

    def onFk(fkFunc: Fields => ForeignKey[Fields2, Row2])(using DummyImplicit): SelectBuilder[Tuple2[Fields, Fields2], Tuple2[Row, Row2]] = {
      parent.joinFk(fields => fkFunc(fields).underlying, other)
    }

    def on(pred: Tuple2[Fields, Fields2] => typo.dsl.SqlExpr[java.lang.Boolean]): SelectBuilder[Tuple2[Fields, Fields2], Tuple2[Row, Row2]] = {
      parent.joinOn(other, tuple => pred(tuple))
    }

    def on(pred: Tuple2[Fields, Fields2] => typo.dsl.SqlExpr[Boolean])(using DummyImplicit): SelectBuilder[Tuple2[Fields, Fields2], Tuple2[Row, Row2]] = {
      parent.joinOn(other, tuple => SqlExpr.toJavaBool(pred(tuple)))
    }

    def leftOn(
        pred: Tuple2[Fields, Fields2] => typo.dsl.SqlExpr[java.lang.Boolean]
    ): LeftJoinSelectBuilder[Fields, Fields2, Row, Row2] = {
      parent.leftJoinOn(other, tuple => pred(tuple))
    }

    def leftOn(
        pred: Tuple2[Fields, Fields2] => typo.dsl.SqlExpr[Boolean]
    )(using DummyImplicit): LeftJoinSelectBuilder[Fields, Fields2, Row, Row2] = {
      parent.leftJoinOn(other, tuple => SqlExpr.toJavaBool(pred(tuple)))
    }
  }
}

/** A specialized SelectBuilder for multiset joins that converts java.util.List to scala.List.
  */
class MultisetSelectBuilder[Fields1, Fields2, Row1, Row2](
    private[scaladsl] val javaBuilder: typo.dsl.SelectBuilder[Tuple2[Fields1, Fields2], Tuple2[Row1, java.util.List[Row2]]]
) {

  def renderCtx(): typo.dsl.RenderCtx = javaBuilder.renderCtx()

  def where(predicate: Tuple2[Fields1, Fields2] => typo.dsl.SqlExpr[java.lang.Boolean]): MultisetSelectBuilder[Fields1, Fields2, Row1, Row2] = {
    new MultisetSelectBuilder(javaBuilder.where(fields => predicate(fields)))
  }

  def maybeWhere[T](value: Option[T], predicate: (Tuple2[Fields1, Fields2], T) => typo.dsl.SqlExpr[java.lang.Boolean]): MultisetSelectBuilder[Fields1, Fields2, Row1, Row2] = {
    new MultisetSelectBuilder(javaBuilder.maybeWhere(value.toJava, (fields: Tuple2[Fields1, Fields2], t: T) => predicate(fields, t)))
  }

  def orderBy[T](orderFunc: Tuple2[Fields1, Fields2] => typo.dsl.SortOrder[T]): MultisetSelectBuilder[Fields1, Fields2, Row1, Row2] = {
    new MultisetSelectBuilder(javaBuilder.orderBy(fields => orderFunc(fields)))
  }

  def offset(offset: Int): MultisetSelectBuilder[Fields1, Fields2, Row1, Row2] = {
    new MultisetSelectBuilder(javaBuilder.offset(offset))
  }

  def limit(limit: Int): MultisetSelectBuilder[Fields1, Fields2, Row1, Row2] = {
    new MultisetSelectBuilder(javaBuilder.limit(limit))
  }

  def toList(implicit connection: Connection): List[(Row1, List[Row2])] = {
    javaBuilder.toList(connection).asScala.toList.map { javaTuple =>
      (javaTuple._1(), javaTuple._2().asScala.toList)
    }
  }

  def count(implicit connection: Connection): Int = {
    javaBuilder.count(connection)
  }

  def sql(): Option[Fragment] = {
    javaBuilder.sql().toScala
  }
}

/** A specialized SelectBuilder for left joins that converts java.util.Optional to scala.Option. This is separate from SelectBuilder because the Row type contains Optional in Java but Option in Scala.
  */
class LeftJoinSelectBuilder[Fields1, Fields2, Row1, Row2](
    private[scaladsl] val javaBuilder: typo.dsl.SelectBuilder[Tuple2[Fields1, Fields2], Tuple2[Row1, java.util.Optional[Row2]]]
) {

  def renderCtx(): typo.dsl.RenderCtx = javaBuilder.renderCtx()

  def where(predicate: Tuple2[Fields1, Fields2] => typo.dsl.SqlExpr[java.lang.Boolean]): LeftJoinSelectBuilder[Fields1, Fields2, Row1, Row2] = {
    new LeftJoinSelectBuilder(javaBuilder.where(fields => predicate(fields)))
  }

  def where(predicate: Tuple2[Fields1, Fields2] => typo.dsl.SqlExpr[Boolean])(using DummyImplicit): LeftJoinSelectBuilder[Fields1, Fields2, Row1, Row2] = {
    new LeftJoinSelectBuilder(javaBuilder.where(fields => SqlExpr.toJavaBool(predicate(fields))))
  }

  def maybeWhere[T](value: Option[T], predicate: (Tuple2[Fields1, Fields2], T) => typo.dsl.SqlExpr[java.lang.Boolean]): LeftJoinSelectBuilder[Fields1, Fields2, Row1, Row2] = {
    new LeftJoinSelectBuilder(javaBuilder.maybeWhere(value.toJava, (fields: Tuple2[Fields1, Fields2], t: T) => predicate(fields, t)))
  }

  def orderBy[T](orderFunc: Tuple2[Fields1, Fields2] => typo.dsl.SortOrder[T]): LeftJoinSelectBuilder[Fields1, Fields2, Row1, Row2] = {
    new LeftJoinSelectBuilder(javaBuilder.orderBy(fields => orderFunc(fields)))
  }

  def offset(offset: Int): LeftJoinSelectBuilder[Fields1, Fields2, Row1, Row2] = {
    new LeftJoinSelectBuilder(javaBuilder.offset(offset))
  }

  def limit(limit: Int): LeftJoinSelectBuilder[Fields1, Fields2, Row1, Row2] = {
    new LeftJoinSelectBuilder(javaBuilder.limit(limit))
  }

  def toList(implicit connection: Connection): List[(Row1, Option[Row2])] = {
    javaBuilder.toList(connection).asScala.toList.map { javaTuple =>
      (javaTuple._1, javaTuple._2.toScala)
    }
  }

  def count(implicit connection: Connection): Int = {
    javaBuilder.count(connection)
  }

  def sql(): Option[Fragment] = {
    javaBuilder.sql().toScala
  }
}

object SelectBuilder {
  def of[Fields, Row](
      name: String,
      structure: typo.dsl.RelationStructure[Fields, Row],
      rowParser: typo.scaladsl.RowParser[Row],
      dialect: typo.dsl.Dialect
  ): SelectBuilder[Fields, Row] = {
    new SelectBuilder(typo.dsl.SelectBuilder.of(name, structure, rowParser.underlying, dialect))
  }
}
