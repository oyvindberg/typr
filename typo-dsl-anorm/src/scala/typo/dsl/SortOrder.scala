package typo.dsl

import typo.dsl.Fragment.FragmentStringInterpolator

import java.util.concurrent.atomic.AtomicInteger

sealed trait SortOrderNoHkt[NT] {
  val expr: SqlExpr.SqlExprNoHkt[NT]
  val ascending: Boolean
  val nullsFirst: Boolean

  def withNullsFirst: SortOrderNoHkt[NT]

  final def render(ctx: RenderCtx, counter: AtomicInteger): Fragment = {
    List[Fragment](
      expr.render(ctx, counter: AtomicInteger),
      if (ascending) frag"ASC" else frag"DESC",
      if (nullsFirst) frag"NULLS FIRST" else Fragment.empty
    ).mkFragment(" ")
  }
}

// sort by a field
final case class SortOrder[T, N[_]](expr: SqlExpr[T, N], ascending: Boolean, nullsFirst: Boolean)(implicit val nullability: Nullability[N]) extends SortOrderNoHkt[N[T]] {
  def withNullsFirst: SortOrder[T, N] = copy(nullsFirst = true)(nullability)
}
