package typo.kotlindsl

import java.sql.ResultSet

/**
 * Kotlin wrapper for typo.runtime.ResultSetParser that provides Kotlin-native methods.
 *
 * Wraps the Java ResultSetParser to provide interop with Java APIs.
 */
class ResultSetParser<Out>(val underlying: typo.runtime.ResultSetParser<Out>) {
    fun apply(rs: ResultSet): Out = underlying.apply(rs)
}

/**
 * Convert a Java ResultSetParser to a Kotlin ResultSetParser.
 */
fun <Out> typo.runtime.ResultSetParser<Out>.asKotlin(): ResultSetParser<Out> {
    return ResultSetParser(this)
}

/**
 * Convert a Kotlin ResultSetParser to a Java ResultSetParser.
 */
fun <Out> ResultSetParser<Out>.asJava(): typo.runtime.ResultSetParser<Out> {
    return underlying
}
