package typo.scaladsl

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

object StaticExports {

  // Type aliases for runtime types
  type Either[L, R] = typo.runtime.Either[L, R]
  type And[T1, T2] = typo.runtime.And[T1, T2]

  // PostgreSQL data wrapper types
  type Json = typo.data.Json
  type Jsonb = typo.data.Jsonb
  type Money = typo.data.Money
  type Xml = typo.data.Xml
  type Vector = typo.data.Vector
  type Record = typo.data.Record
  type Unknown = typo.data.Unknown
  type Xid = typo.data.Xid
  type Inet = typo.data.Inet
  type AclItem = typo.data.AclItem
  type AnyArray = typo.data.AnyArray
  type Int2Vector = typo.data.Int2Vector
  type OidVector = typo.data.OidVector
  type PgNodeTree = typo.data.PgNodeTree

  // Regclass types
  type Regclass = typo.data.Regclass
  type Regconfig = typo.data.Regconfig
  type Regdictionary = typo.data.Regdictionary
  type Regnamespace = typo.data.Regnamespace
  type Regoper = typo.data.Regoper
  type Regoperator = typo.data.Regoperator
  type Regproc = typo.data.Regproc
  type Regprocedure = typo.data.Regprocedure
  type Regrole = typo.data.Regrole
  type Regtype = typo.data.Regtype

  // Range types
  type Range[T <: Comparable[T]] = typo.data.Range[T]
  type RangeBound[T <: Comparable[T]] = typo.data.RangeBound[T]
  type RangeFinite[T <: Comparable[T]] = typo.data.RangeFinite[T]

  // Array type
  type Arr[A] = typo.data.Arr[A]

  // Core type system
  type PgType[A] = typo.runtime.PgType[A]
  type PgTypename[A] = typo.runtime.PgTypename[A]
  type PgRead[A] = typo.runtime.PgRead[A]
  type PgWrite[A] = typo.runtime.PgWrite[A]
  type PgText[A] = typo.runtime.PgText[A]

  // Database access types
  type Transactor = typo.runtime.Transactor

  // Functional interfaces
  type SqlFunction[T, R] = typo.runtime.SqlFunction[T, R]
  type SqlConsumer[T] = typo.runtime.SqlConsumer[T]
  type SqlBiConsumer[T1, T2] = typo.runtime.SqlBiConsumer[T1, T2]

  // Utility
  type ByteArrays = typo.runtime.internal.ByteArrays
  type ArrParser = typo.runtime.ArrParser

  object Types {
    // Primitive types
    val bool: PgType[java.lang.Boolean] = typo.runtime.PgTypes.bool
    val int2: PgType[java.lang.Short] = typo.runtime.PgTypes.int2
    val int4: PgType[java.lang.Integer] = typo.runtime.PgTypes.int4
    val int8: PgType[java.lang.Long] = typo.runtime.PgTypes.int8
    val float4: PgType[java.lang.Float] = typo.runtime.PgTypes.float4
    val float8: PgType[java.lang.Double] = typo.runtime.PgTypes.float8
    val numeric: PgType[java.math.BigDecimal] = typo.runtime.PgTypes.numeric
    val text: PgType[String] = typo.runtime.PgTypes.text
    val bytea: PgType[Array[Byte]] = typo.runtime.PgTypes.bytea
    val uuid: PgType[java.util.UUID] = typo.runtime.PgTypes.uuid

    // Date/time types
    val date: PgType[java.time.LocalDate] = typo.runtime.PgTypes.date
    val time: PgType[java.time.LocalTime] = typo.runtime.PgTypes.time
    val timestamp: PgType[java.time.LocalDateTime] = typo.runtime.PgTypes.timestamp
    val timestamptz: PgType[java.time.Instant] = typo.runtime.PgTypes.timestamptz
    val interval: PgType[org.postgresql.util.PGInterval] = typo.runtime.PgTypes.interval

    // JSON types
    val json: PgType[Json] = typo.runtime.PgTypes.json
    val jsonb: PgType[Jsonb] = typo.runtime.PgTypes.jsonb

    // Other types
    val xml: PgType[Xml] = typo.runtime.PgTypes.xml
    val money: PgType[Money] = typo.runtime.PgTypes.money
    val inet: PgType[Inet] = typo.runtime.PgTypes.inet
    val vector: PgType[Vector] = typo.runtime.PgTypes.vector
    val xid: PgType[Xid] = typo.runtime.PgTypes.xid

    // Reg* types
    val regclass: PgType[Regclass] = typo.runtime.PgTypes.regclass
    val regconfig: PgType[Regconfig] = typo.runtime.PgTypes.regconfig
    val regdictionary: PgType[Regdictionary] = typo.runtime.PgTypes.regdictionary
    val regnamespace: PgType[Regnamespace] = typo.runtime.PgTypes.regnamespace
    val regoper: PgType[Regoper] = typo.runtime.PgTypes.regoper
    val regoperator: PgType[Regoperator] = typo.runtime.PgTypes.regoperator
    val regproc: PgType[Regproc] = typo.runtime.PgTypes.regproc
    val regprocedure: PgType[Regprocedure] = typo.runtime.PgTypes.regprocedure
    val regrole: PgType[Regrole] = typo.runtime.PgTypes.regrole
    val regtype: PgType[Regtype] = typo.runtime.PgTypes.regtype

    // Factory methods
    def ofEnum[E <: Enum[E]](name: String, fromString: String => E): PgType[E] = {
      typo.runtime.PgTypes.ofEnum(name, s => fromString(s))
    }

    def ofPgObject[T](
        sqlType: String,
        constructor: String => T,
        extractor: T => String,
        json: typo.runtime.PgJson[T]
    ): PgType[T] = {
      typo.runtime.PgTypes.ofPgObject(sqlType, s => constructor(s), t => extractor(t), json)
    }

    def bpchar(precision: Int): PgType[String] = {
      typo.runtime.PgTypes.bpchar(precision)
    }

    def record(typename: String): PgType[Record] = {
      typo.runtime.PgTypes.record(typename)
    }
  }

  // Export Fragment and Operation wrappers
  export typo.scaladsl.Fragment
  export typo.scaladsl.Fragment.{sql as _, *}
  export typo.scaladsl.Operation

  object Parsers {
    def all[Out](rowParser: typo.runtime.RowParser[Out]): typo.scaladsl.ResultSetParser[List[Out]] = {
      val javaParser = new typo.runtime.ResultSetParser.All(rowParser)
      new typo.scaladsl.ResultSetParser(new typo.runtime.ResultSetParser[List[Out]] {
        override def apply(rs: java.sql.ResultSet): List[Out] = javaParser.apply(rs).asScala.toList
      })
    }

    def first[Out](rowParser: typo.runtime.RowParser[Out]): typo.scaladsl.ResultSetParser[Option[Out]] = {
      val javaParser = new typo.runtime.ResultSetParser.First(rowParser)
      new typo.scaladsl.ResultSetParser(new typo.runtime.ResultSetParser[Option[Out]] {
        override def apply(rs: java.sql.ResultSet): Option[Out] = javaParser.apply(rs).toScala
      })
    }

    def maxOne[Out](rowParser: typo.runtime.RowParser[Out]): typo.scaladsl.ResultSetParser[Option[Out]] = {
      val javaParser = new typo.runtime.ResultSetParser.MaxOne(rowParser)
      new typo.scaladsl.ResultSetParser(new typo.runtime.ResultSetParser[Option[Out]] {
        override def apply(rs: java.sql.ResultSet): Option[Out] = javaParser.apply(rs).toScala
      })
    }

    def exactlyOne[Out](rowParser: typo.runtime.RowParser[Out]): typo.scaladsl.ResultSetParser[Out] = {
      val javaParser = new typo.runtime.ResultSetParser.ExactlyOne(rowParser)
      new typo.scaladsl.ResultSetParser(javaParser)
    }

    def foreach[Out](rowParser: typo.runtime.RowParser[Out], consumer: Out => Unit): typo.scaladsl.ResultSetParser[Unit] = {
      val foreachParser = new typo.runtime.ResultSetParser.Foreach(rowParser, c => consumer(c))
      // Wrap Void-returning parser to return Unit
      val unitParser = new typo.runtime.ResultSetParser[Unit] {
        override def apply(rs: java.sql.ResultSet): Unit = {
          foreachParser.apply(rs)
          ()
        }
      }
      new typo.scaladsl.ResultSetParser(unitParser)
    }
  }
}
