package typo.dsl

import typo.dsl.internal.DummyOrdering

sealed trait OrderByOrSeek[Fields, NT] {
  val f: Fields => SortOrderNoHkt[NT]
}

object OrderByOrSeek {
  case class OrderBy[Fields, NT](f: Fields => SortOrderNoHkt[NT]) extends OrderByOrSeek[Fields, NT]
  sealed trait SeekNoHkt[Fields, NT] extends OrderByOrSeek[Fields, NT]
  case class Seek[Fields, T, N[_]](f: Fields => SortOrder[T, N], value: SqlExpr.Const[T, N]) extends SeekNoHkt[Fields, N[T]]

  def expand[Fields, Row](fields: Fields, params: SelectParams[Fields, Row]): (List[SqlExpr[Boolean, Option]], List[SortOrderNoHkt[?]]) = {
    val seeks: List[SeekNoHkt[Fields, ?]] =
      params.orderBy.collect { case x: OrderByOrSeek.SeekNoHkt[Fields, nt] => x }

    val maybeSeekPredicate: Option[SqlExpr[Boolean, Option]] =
      seeks match {
        case Nil => None
        case nonEmpty =>
          val seekOrderBys: List[SortOrderNoHkt[?]] =
            nonEmpty.map { case seek: Seek[Fields, _, _] @unchecked /* for 2.13*/ => seek.f(fields) }

          seekOrderBys.map(_.ascending).distinct match {
            case List(uniformIsAscending) =>
              val dbTuple = SqlExpr.RowExpr(seekOrderBys.map(so => so.expr))
              val valueTuple = SqlExpr.RowExpr(nonEmpty.map { case seek: Seek[Fields, t, n] @unchecked /* for 2.13 */ => seek.value })
              // this is for mock repositories. it's hard to separate it out from here
              implicit val ordering: Ordering[List[?]] = (leftRow: List[?], rightRow: List[?]) =>
                leftRow.iterator
                  .zip(rightRow.iterator)
                  .zip(seekOrderBys.iterator)
                  .map { case ((left, right), so: SortOrder[t, n] @unchecked) /* for 2.13*/ =>
                    val ordering: Ordering[Option[t]] = Ordering.Option(if (so.ascending) DummyOrdering.ord[t] else DummyOrdering.ord[t].reverse)
                    ordering.compare(so.nullability.toOpt(left.asInstanceOf[n[t]]), so.nullability.toOpt(right.asInstanceOf[n[t]]))
                  }
                  .find(_ != 0)
                  .getOrElse(0)

              Some(if (uniformIsAscending) dbTuple > valueTuple else dbTuple < valueTuple)
            case _ =>
              val orConditions: Seq[SqlExpr[Boolean, Option]] =
                nonEmpty.indices.map { i =>
                  val equals: List[SqlExpr[Boolean, Option]] =
                    nonEmpty.take(i).map { case seek: Seek[Fields, t, n] @unchecked /* for 2.13*/ =>
                      val so: SortOrder[t, n] = seek.f(fields)
                      implicit val n: Nullability2[n, n, Option] = Nullability2.opt(using so.nullability, so.nullability)
                      so.expr === seek.value
                    }

                  nonEmpty(i) match {
                    case seek: Seek[Fields, t, n] @unchecked /* for 2.13*/ =>
                      val so: SortOrder[t, n] = seek.f(fields)
                      implicit val n: Nullability2[n, n, Option] = Nullability2.opt(using so.nullability, so.nullability)
                      val predicate: SqlExpr[Boolean, Option] =
                        if (so.ascending) so.expr > seek.value else so.expr < seek.value
                      (equals :+ predicate).reduce(_ and _)
                  }
                }

              Some(orConditions.reduce(_ or _))
          }
      }
    (params.where.map(_.apply(fields)) ++ maybeSeekPredicate, params.orderBy.map(_.f(fields)))
  }
}
