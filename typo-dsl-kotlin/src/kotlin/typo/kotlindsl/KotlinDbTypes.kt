package typo.kotlindsl

import typo.runtime.MariaType
import typo.runtime.PgType
import typo.runtime.SqlFunction

/**
 * Kotlin-friendly DbType instances that use Kotlin types instead of Java boxed types.
 */
object KotlinDbTypes {
    object PgTypes {
        // Primitives - convert Java boxed types to Kotlin native types
        val bool: PgType<Boolean> = typo.runtime.PgTypes.bool.bimap(
            SqlFunction { it },
            { it }
        )
        val int2: PgType<Short> = typo.runtime.PgTypes.int2.bimap(
            SqlFunction { it },
            { it }
        )
        val smallint: PgType<Short> = typo.runtime.PgTypes.smallint.bimap(
            SqlFunction { it },
            { it }
        )
        val int4: PgType<Int> = typo.runtime.PgTypes.int4.bimap(
            SqlFunction { it },
            { it }
        )
        val int8: PgType<Long> = typo.runtime.PgTypes.int8.bimap(
            SqlFunction { it },
            { it }
        )
        val float4: PgType<Float> = typo.runtime.PgTypes.float4.bimap(
            SqlFunction { it },
            { it }
        )
        val float8: PgType<Double> = typo.runtime.PgTypes.float8.bimap(
            SqlFunction { it },
            { it }
        )

        // Collections - convert Java collections to Kotlin collections
        val hstore: PgType<Map<String, String>> = typo.runtime.PgTypes.hstore.bimap(
            SqlFunction { javaMap -> javaMap.toMap() },
            { kotlinMap -> kotlinMap.toMap(java.util.HashMap()) }
        )
    }

    object MariaTypes {
        // Primitives - convert Java boxed types to Kotlin native types
        val tinyint: MariaType<Byte> = typo.runtime.MariaTypes.tinyint.bimap(
            SqlFunction { it },
            { it }
        )
        val smallint: MariaType<Short> = typo.runtime.MariaTypes.smallint.bimap(
            SqlFunction { it },
            { it }
        )
        val mediumint: MariaType<Int> = typo.runtime.MariaTypes.mediumint.bimap(
            SqlFunction { it },
            { it }
        )
        val int_: MariaType<Int> = typo.runtime.MariaTypes.int_.bimap(
            SqlFunction { it },
            { it }
        )
        val bigint: MariaType<Long> = typo.runtime.MariaTypes.bigint.bimap(
            SqlFunction { it },
            { it }
        )

        // Unsigned integers
        val tinyintUnsigned: MariaType<Short> = typo.runtime.MariaTypes.tinyintUnsigned.bimap(
            SqlFunction { it },
            { it }
        )
        val smallintUnsigned: MariaType<Int> = typo.runtime.MariaTypes.smallintUnsigned.bimap(
            SqlFunction { it },
            { it }
        )
        val mediumintUnsigned: MariaType<Int> = typo.runtime.MariaTypes.mediumintUnsigned.bimap(
            SqlFunction { it },
            { it }
        )
        val intUnsigned: MariaType<Long> = typo.runtime.MariaTypes.intUnsigned.bimap(
            SqlFunction { it },
            { it }
        )

        // Floating point
        val float_: MariaType<Float> = typo.runtime.MariaTypes.float_.bimap(
            SqlFunction { it },
            { it }
        )
        val double_: MariaType<Double> = typo.runtime.MariaTypes.double_.bimap(
            SqlFunction { it },
            { it }
        )

        // Boolean
        val bool: MariaType<Boolean> = typo.runtime.MariaTypes.bool.bimap(
            SqlFunction { it },
            { it }
        )
        val bit1: MariaType<Boolean> = typo.runtime.MariaTypes.bit1.bimap(
            SqlFunction { it },
            { it }
        )
    }
}
