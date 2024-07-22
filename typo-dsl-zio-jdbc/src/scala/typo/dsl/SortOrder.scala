package typo.dsl

import zio.Chunk
import zio.jdbc.*

sealed trait SortOrderNoHkt[NT] {
  val expr: SqlExpr.SqlExprNoHkt[NT]
  val ascending: Boolean
  val nullsFirst: Boolean

  def withNullsFirst: SortOrderNoHkt[NT]

  final def render(ctx: RenderCtx): SqlFragment = {
    Chunk(
      expr.render(ctx),
      if (ascending) sql"ASC" else sql"DESC",
      if (nullsFirst) sql"NULLS FIRST" else SqlFragment.empty
    ).mkFragment(" ")
  }
}

// sort by a field
final case class SortOrder[T, N[_]](expr: SqlExpr[T, N], ascending: Boolean, nullsFirst: Boolean)(implicit val nullability: Nullability[N]) extends SortOrderNoHkt[N[T]] {
  def withNullsFirst: SortOrder[T, N] = copy(nullsFirst = true)(nullability)
}
