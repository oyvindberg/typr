package typo.dsl.internal

import typo.dsl.{SortOrder, SortOrderNoHkt, Structure}

object mocks {
  class RowOrdering[Fields, Row](structure: Structure[Fields, Row], sortOrderings: List[SortOrderNoHkt[?]]) extends Ordering[Row] {
    override def compare(leftRow: Row, rightRow: Row): Int =
      sortOrderings.iterator
        .map { case so: SortOrder[t, n] @unchecked /* for 2.13*/ =>
          val left = structure.untypedEval(so.expr, leftRow)
          val right = structure.untypedEval(so.expr, rightRow)
          val ordering: Ordering[Option[t]] = Ordering.Option(if (so.ascending) DummyOrdering.ord[t] else DummyOrdering.ord[t].reverse)
          ordering.compare(so.nullability.toOpt(left), so.nullability.toOpt(right))
        }
        .find(_ != 0)
        .getOrElse(0)
  }
}
