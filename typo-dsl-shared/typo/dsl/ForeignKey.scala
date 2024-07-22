package typo.dsl

case class ForeignKey[Fields2, Row2](
    name: String,
    columnPairs: List[ForeignKey.ColumnPair[?, Fields2]]
) {

  def withColumnPair[T, ThisN[_]: Nullability, ThatN[_]: Nullability](
      field: SqlExpr.FieldLike[T, ThisN, ?],
      thatField: Fields2 => SqlExpr.FieldLike[T, ThatN, Row2]
  ): ForeignKey[Fields2, Row2] =
    new ForeignKey[Fields2, Row2](
      name,
      columnPairs :+ ForeignKey.ColumnPair[T, Fields2](field.?, f => thatField(f).?)
    )
}

object ForeignKey {
  case class ColumnPair[T, Fields2](
      thisField: SqlExpr[T, Option],
      thatField: Fields2 => SqlExpr[T, Option],
  )
}
