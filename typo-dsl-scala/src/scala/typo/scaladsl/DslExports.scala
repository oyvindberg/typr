package typo.scaladsl

import scala.jdk.CollectionConverters._

object DslExports {

  // Type aliases for DSL types
  type Bijection[Wrapper, Underlying] = typo.dsl.Bijection[Wrapper, Underlying]
  type SortOrder[T] = typo.dsl.SortOrder[T]

  // Functional interfaces
  type SqlFunction2[T1, T2, R] = typo.dsl.SqlFunction2[T1, T2, R]
  type SqlFunction3[T1, T2, T3, R] = typo.dsl.SqlFunction3[T1, T2, T3, R]
  type TriFunction[T1, T2, T3, R] = typo.dsl.TriFunction[T1, T2, T3, R]

  // Builder parameter types
  type DeleteParams[Fields] = typo.dsl.DeleteParams[Fields]
  type SelectParams[Fields, Row] = typo.dsl.SelectParams[Fields, Row]
  type UpdateParams[Fields, Row] = typo.dsl.UpdateParams[Fields, Row]

  // Path type
  type Path = typo.dsl.Path

  // Mock builder functions
  def SelectBuilderMock[Fields, Row](
      structure: typo.dsl.RelationStructure[Fields, Row],
      allRowsSupplier: () => List[Row],
      params: SelectParams[Fields, Row]
  ): SelectBuilder[Fields, Row] = {
    new SelectBuilder(
      typo.dsl.SelectBuilderMock(
        structure,
        () => allRowsSupplier().asJava,
        params
      )
    )
  }

  def DeleteBuilderMock[Id, Fields, Row](
      structure: typo.dsl.RelationStructure[Fields, Row],
      allRowsSupplier: () => List[Row],
      params: DeleteParams[Fields],
      idExtractor: Row => Id,
      deleteById: Id => Unit
  ): DeleteBuilder[Fields, Row] = {
    new DeleteBuilder(
      typo.dsl.DeleteBuilderMock(
        structure,
        () => allRowsSupplier().asJava,
        params,
        (row: Row) => idExtractor(row),
        (id: Id) => deleteById(id)
      )
    )
  }

  def UpdateBuilderMock[Fields, Row](
      structure: typo.dsl.RelationStructure[Fields, Row],
      allRowsSupplier: () => List[Row],
      params: UpdateParams[Fields, Row],
      copyRow: Row => Row
  ): UpdateBuilder[Fields, Row] = {
    new UpdateBuilder(
      typo.dsl.UpdateBuilderMock(
        structure,
        () => allRowsSupplier().asJava,
        params,
        (row: Row) => copyRow(row)
      )
    )
  }

  // SortOrder extension methods
  implicit class SqlExprSortOrderOps[T](private val expr: typo.dsl.SqlExpr[T]) extends AnyVal {
    def asc(): SortOrder[T] = typo.dsl.SortOrder.asc(expr)
    def desc(): SortOrder[T] = typo.dsl.SortOrder.desc(expr)
  }
}
