package dev.typr.foundations.kotlin

import dev.typr.foundations.*
import java.util.Optional

/**
 * Kotlin extension methods for typr-runtime-java that provide:
 * - Nullable types instead of Optional
 * - Kotlin-friendly lambda syntax
 * - Better generic type handling
 */

// ================================
// DbType Extensions
// ================================

/**
 * Kotlin-friendly nullable version of DbType.opt().
 *
 * Java's DbType.opt() returns DbType<Optional<A>>, but Kotlin code should work with A? instead.
 * This extension wraps a DbType<A> to create a DbType<A?> that converts between Optional and nullable
 * at read/write boundaries.
 *
 * Usage:
 *   val nullableText: DbType<String?> = PgTypes.text.nullable()
 *
 * Instead of:
 *   val optionalText: DbType<Optional<String>> = PgTypes.text.opt()  // Java-style
 */
fun <A : Any> DbType<A>.nullable(): DbType<A?> =
    this.opt().to(optionalToNullable())

// ================================
// Either Extensions (value-level)
// ================================

/**
 * Convert Either<L, R> to R? (nullable right value).
 * Uses .orNull() from OptionalExtensions.
 */
fun <L, R> Either<L, R>.rightOrNull(): R? {
    return this.asOptional().orNull()
}

/**
 * Get left value or null.
 */
fun <L, R> Either<L, R>.leftOrNull(): L? {
    return when (this) {
        is dev.typr.foundations.Either.Left -> this.value()
        else -> null
    }
}

// ================================
// Arr Extensions (value-level)
// ================================

/**
 * Reshape array to new dimensions or return null.
 * Uses .orNull() from OptionalExtensions.
 */
fun <A> Arr<A>.reshapeOrNull(vararg newDims: Int): Arr<A>? {
    return this.reshape(*newDims).orNull()
}

/**
 * Get array element at indices or return null.
 * Uses .orNull() from OptionalExtensions.
 */
fun <A> dev.typr.foundations.data.Arr<A>.getOrNull(vararg indices: Int): A? {
    return this.get(*indices).orNull()
}

// ================================
// Range Extensions (value-level)
// ================================

/**
 * Get finite range or null.
 * Uses .orNull() from OptionalExtensions.
 */
fun <T : Comparable<T>> Range<T>.finiteOrNull(): RangeFinite<T>? {
    return this.finite().orNull()
}

// ================================
// Fragment Extensions
// ================================

/**
 * Build a Fragment using Kotlin DSL.
 */
inline fun buildFragment(block: dev.typr.foundations.Fragment.Builder.() -> Unit): Fragment {
    val builder = dev.typr.foundations.Fragment.Builder()
    builder.block()
    return Fragment(builder.done())
}

/**
 * Append a nullable parameter to fragment builder.
 * Converts Kotlin's T? to Java's Optional<T> automatically.
 */
fun <T> dev.typr.foundations.Fragment.Builder.paramNullable(type: DbType<T>, value: T?): dev.typr.foundations.Fragment.Builder {
    return this.param(type.opt(), value.toOptional())
}

/**
 * Kotlin-friendly query method that accepts Kotlin ResultSetParser.
 * Converts Kotlin ResultSetParser to Java ResultSetParser automatically.
 */
fun <Out> dev.typr.foundations.Fragment.query(parser: ResultSetParser<Out>): dev.typr.foundations.Operation<Out> {
    return this.query(parser.underlying)
}

// ================================
// Operation Extensions
// ================================

/**
 * Extension to convert Operation<Optional<T>> results to T? automatically.
 * This handles the common case where Java methods return Optional but Kotlin code expects nullable types.
 */
fun <T> dev.typr.foundations.Operation<java.util.Optional<T>>.runUncheckedOrNull(c: java.sql.Connection): T? {
    return this.runUnchecked(c).orNull()
}
