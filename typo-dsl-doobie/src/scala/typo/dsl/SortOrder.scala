package typo.dsl

import cats.syntax.foldable.*
import doobie.implicits.toSqlInterpolator
import doobie.util.fragment.Fragment

sealed trait SortOrderNoHkt[NT] {
  val expr: SqlExpr.SqlExprNoHkt[NT]
  val ascending: Boolean
  val nullsFirst: Boolean

  def withNullsFirst: SortOrderNoHkt[NT]

  def render(ctx: RenderCtx): Fragment =
    List(
      expr.render(ctx),
      if (ascending) fr"ASC" else fr"DESC",
      if (nullsFirst) fr"NULLS FIRST" else Fragment.empty
    ).intercalate(fr" ")
}

// sort by a field
final case class SortOrder[T, N[_]](expr: SqlExpr[T, N], ascending: Boolean, nullsFirst: Boolean)(implicit val nullability: Nullability[N]) extends SortOrderNoHkt[N[T]] {
  def withNullsFirst: SortOrder[T, N] = copy(nullsFirst = true)(nullability)
}
