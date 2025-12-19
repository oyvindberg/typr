package typo.kotlindsl

// ================================
// DSL Type Aliases
// ================================

// Core DSL types
typealias Bijection<Wrapper, Underlying> = typo.dsl.Bijection<Wrapper, Underlying>
typealias Tuple2<A, B> = typo.dsl.Tuple2<A, B>
// Note: SqlExpr and Structure are defined as objects below for nested type access
typealias SortOrder<T> = typo.dsl.SortOrder<T>
typealias Dialect = typo.dsl.Dialect
typealias FieldsExpr<Row> = typo.dsl.FieldsExpr<Row>

// Functional interfaces (for SQL expressions with multiple parameters)
typealias SqlFunction2<T1, T2, R> = typo.dsl.SqlFunction2<T1, T2, R>
typealias SqlFunction3<T1, T2, T3, R> = typo.dsl.SqlFunction3<T1, T2, T3, R>
typealias TriFunction<T1, T2, T3, R> = typo.dsl.TriFunction<T1, T2, T3, R>

// Builder parameter types (used by generated code)
typealias DeleteParams<Fields> = typo.dsl.DeleteParams<Fields>
typealias SelectParams<Fields, Row> = typo.dsl.SelectParams<Fields, Row>
typealias UpdateParams<Fields, Row> = typo.dsl.UpdateParams<Fields, Row>

// Top-level mock constructor functions (Kotlin can't import companion object members)
// These forward to the companion object functions to maintain Java DSL structure

fun <Fields, Row> SelectBuilderMock(
    structure: typo.dsl.RelationStructure<Fields, Row>,
    allRowsSupplier: () -> List<Row>,
    params: SelectParams<Fields, Row>
): SelectBuilder<Fields, Row> = SelectBuilder(
    typo.dsl.SelectBuilderMock(
        structure,
        java.util.function.Supplier { allRowsSupplier() },
        params
    )
)

fun <Id, Fields, Row> DeleteBuilderMock(
    structure: typo.dsl.RelationStructure<Fields, Row>,
    allRowsSupplier: () -> List<Row>,
    params: DeleteParams<Fields>,
    idExtractor: (Row) -> Id,
    deleteById: (Id) -> Unit
): DeleteBuilder<Fields, Row> = DeleteBuilder(
    typo.dsl.DeleteBuilderMock(
        structure,
        java.util.function.Supplier { allRowsSupplier() },
        params,
        java.util.function.Function { row -> idExtractor(row) },
        java.util.function.Consumer { id -> deleteById(id) }
    )
)

fun <Fields, Row> UpdateBuilderMock(
    structure: typo.dsl.RelationStructure<Fields, Row>,
    allRowsSupplier: () -> List<Row>,
    params: UpdateParams<Fields, Row>,
    copyRow: (Row) -> Row
): UpdateBuilder<Fields, Row> = UpdateBuilder(
    typo.dsl.UpdateBuilderMock(
        structure,
        java.util.function.Supplier { allRowsSupplier() },
        params,
        java.util.function.Function { row -> copyRow(row) }
    )
)

// Path type
typealias Path = typo.dsl.Path

// Note: ForeignKey wrapper class is defined in SqlExpr.kt
// Note: SqlExpr and Structure objects are defined in separate files
// SqlExpr.kt and Structure.kt for better organization

// ================================
// SortOrder Extensions
// ================================

/**
 * Create ascending sort order from SqlExpr.
 * Usage: field.asc()
 */
fun <T> typo.dsl.SqlExpr<T>.asc(): SortOrder<T> = typo.dsl.SortOrder.asc(this)

/**
 * Create descending sort order from SqlExpr.
 * Usage: field.desc()
 */
fun <T> typo.dsl.SqlExpr<T>.desc(): SortOrder<T> = typo.dsl.SortOrder.desc(this)
