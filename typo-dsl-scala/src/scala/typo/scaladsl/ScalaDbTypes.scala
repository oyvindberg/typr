package typo.scaladsl

import typo.runtime.{MariaType, PgType}

import scala.jdk.CollectionConverters.*

/** Scala-friendly DbType instances that use Scala types instead of Java boxed types.
  */
object ScalaDbTypes {
  object PgTypes {
    // Primitives - convert Java boxed types to Scala native types
    val bool: PgType[Boolean] = typo.runtime.PgTypes.bool.bimap(b => b, b => b)
    val int2: PgType[Short] = typo.runtime.PgTypes.int2.bimap(s => s, s => s)
    val smallint: PgType[Short] = typo.runtime.PgTypes.smallint.bimap(s => s, s => s)
    val int4: PgType[Int] = typo.runtime.PgTypes.int4.bimap(i => i, i => i)
    val int8: PgType[Long] = typo.runtime.PgTypes.int8.bimap(l => l, l => l)
    val float4: PgType[Float] = typo.runtime.PgTypes.float4.bimap(f => f, f => f)
    val float8: PgType[Double] = typo.runtime.PgTypes.float8.bimap(d => d, d => d)

    // BigDecimal - convert Java BigDecimal to Scala BigDecimal
    val numeric: PgType[BigDecimal] = typo.runtime.PgTypes.numeric.bimap(jbd => BigDecimal(jbd), sbd => sbd.bigDecimal)

    // Collections - convert Java collections to Scala collections
    val hstore: PgType[Map[String, String]] = typo.runtime.PgTypes.hstore.bimap(javaMap => javaMap.asScala.toMap, scalaMap => scalaMap.asJava)

    // Array types - convert Java boxed arrays to Scala native arrays
    val boolArray: PgType[Array[Boolean]] = typo.runtime.PgTypes.boolArray.bimap(
      arr => arr.map(_.booleanValue()),
      arr => arr.map(java.lang.Boolean.valueOf)
    )
    val int2Array: PgType[Array[Short]] = typo.runtime.PgTypes.int2Array.bimap(
      arr => arr.map(_.shortValue()),
      arr => arr.map(java.lang.Short.valueOf)
    )
    val smallintArray: PgType[Array[Short]] = int2Array
    val int4Array: PgType[Array[Int]] = typo.runtime.PgTypes.int4Array.bimap(
      arr => arr.map(_.intValue()),
      arr => arr.map(java.lang.Integer.valueOf)
    )
    val int8Array: PgType[Array[Long]] = typo.runtime.PgTypes.int8Array.bimap(
      arr => arr.map(_.longValue()),
      arr => arr.map(java.lang.Long.valueOf)
    )
    val float4Array: PgType[Array[Float]] = typo.runtime.PgTypes.float4Array.bimap(
      arr => arr.map(_.floatValue()),
      arr => arr.map(java.lang.Float.valueOf)
    )
    val float8Array: PgType[Array[Double]] = typo.runtime.PgTypes.float8Array.bimap(
      arr => arr.map(_.doubleValue()),
      arr => arr.map(java.lang.Double.valueOf)
    )
    val numericArray: PgType[Array[BigDecimal]] = typo.runtime.PgTypes.numericArray.bimap(
      arr => arr.map(BigDecimal(_)),
      arr => arr.map(_.bigDecimal)
    )
  }

  object MariaTypes {
    // Primitives - convert Java boxed types to Scala native types
    val tinyint: MariaType[Byte] = typo.runtime.MariaTypes.tinyint.bimap(b => b, b => b)
    val smallint: MariaType[Short] = typo.runtime.MariaTypes.smallint.bimap(s => s, s => s)
    val mediumint: MariaType[Int] = typo.runtime.MariaTypes.mediumint.bimap(i => i, i => i)
    val int_ : MariaType[Int] = typo.runtime.MariaTypes.int_.bimap(i => i, i => i)
    val bigint: MariaType[Long] = typo.runtime.MariaTypes.bigint.bimap(l => l, l => l)

    // Unsigned integers
    val tinyintUnsigned: MariaType[Short] = typo.runtime.MariaTypes.tinyintUnsigned.bimap(s => s, s => s)
    val smallintUnsigned: MariaType[Int] = typo.runtime.MariaTypes.smallintUnsigned.bimap(i => i, i => i)
    val mediumintUnsigned: MariaType[Int] = typo.runtime.MariaTypes.mediumintUnsigned.bimap(i => i, i => i)
    val intUnsigned: MariaType[Long] = typo.runtime.MariaTypes.intUnsigned.bimap(l => l, l => l)

    // Floating point
    val float_ : MariaType[Float] = typo.runtime.MariaTypes.float_.bimap(f => f, f => f)
    val double_ : MariaType[Double] = typo.runtime.MariaTypes.double_.bimap(d => d, d => d)

    // BigDecimal - convert Java BigDecimal to Scala BigDecimal
    val numeric: MariaType[BigDecimal] = typo.runtime.MariaTypes.numeric.bimap(jbd => BigDecimal(jbd), sbd => sbd.bigDecimal)

    // Boolean
    val bool: MariaType[Boolean] = typo.runtime.MariaTypes.bool.bimap(b => b, b => b)
    val bit1: MariaType[Boolean] = typo.runtime.MariaTypes.bit1.bimap(b => b, b => b)
  }
}
