---
title: Typo types
---

Typo generates some helper types if they are needed by your database schema.

There are multiple reasons why this is sometimes necessary:
- PostgreSQL driver has broken handling of offset date/time types. need to go through string in order to not lose offset/precision
- money type is broken in many interesting ways
- possibility of transferring instances of a `Row` type to a platform which doesn't have the PostgreSQL driver jar available
- a type to hang otherwise orphan type classes on

## (In-)convenience
The goal here is type-safety, not convenience. 

You'll very likely map row types into your domain objects anyway, so mapping these types to what you want is typically trivial.
The row types belong in the database layer, and so do these Typo types.


That said, these types are primarily driven by the (self-imposed) constraint that typeclass instances be coherent -
That is only one instance per type. This may change in the future.

## Full list of types

```scala
import java.time.*

/** aclitem (via PGObject) */
case class TypoAclItem(value: String)
/** anyarray (via PGObject) */
case class TypoAnyArray(value: String)
/** This represents the box datatype in PostgreSQL */
case class TypoBox(x1: Double, y1: Double, x2: Double, y2: Double)
/** This represents the bytea datatype in PostgreSQL */
case class TypoBytea(value: Array[Byte])
/** This represents circle datatype in PostgreSQL, consisting of a point and a radius */
case class TypoCircle(center: TypoPoint, radius: Double)
/** The text representation of an hstore, used for input and output, includes zero or more key => value pairs separated by commas */
case class TypoHStore(value: Map[String, String])
/** inet (via PGObject) */
case class TypoInet(value: String)
/** This is `java.time.TypoInstant`, but with microsecond precision and transferred to and from postgres as strings. The reason is that postgres driver and db libs are broken */
case class TypoInstant(value: Instant)
/** int2vector (via PGObject) */
case class TypoInt2Vector(value: String)
/** Interval type in PostgreSQL */
case class TypoInterval(years: Int, months: Int, days: Int, hours: Int, minutes: Int, seconds: Double)
/** json (via PGObject) */
case class TypoJson(value: String)
/** jsonb (via PGObject) */
case class TypoJsonb(value: String)
/** This implements a line represented by the linear equation Ax + By + C = 0 */
case class TypoLine(a: Double, b: Double, c: Double)
/** This implements a line represented by the linear equation Ax + By + C = 0 */
case class TypoLineSegment(p1: TypoPoint, p2: TypoPoint)
/** This is `java.time.LocalDate`, but transferred to and from postgres as strings. The reason is that postgres driver and db libs are broken */
case class TypoLocalDate(value: LocalDate)
/** This is `java.time.LocalDateTime`, but with microsecond precision and transferred to and from postgres as strings. The reason is that postgres driver and db libs are broken */
case class TypoLocalDateTime(value: LocalDateTime)
/** This is `java.time.LocalTime`, but with microsecond precision and transferred to and from postgres as strings. The reason is that postgres driver and db libs are broken */
case class TypoLocalTime(value: LocalTime)
/** Money and cash types in PostgreSQL */
case class TypoMoney(value: BigDecimal)
/** This is `java.time.OffsetTime`, but with microsecond precision and transferred to and from postgres as strings. The reason is that postgres driver and db libs are broken */
case class TypoOffsetTime(value: OffsetTime)
/** oidvector (via PGObject) */
case class TypoOidVector(value: String)
/** This implements a path (a multiple segmented line, which may be closed) */
case class TypoPath(open: Boolean, points: List[TypoPoint])
/** pg_node_tree (via PGObject) */
case class TypoPgNodeTree(value: String)
/** Point datatype in PostgreSQL */
case class TypoPoint(x: Double, y: Double)
/** Polygon datatype in PostgreSQL */
case class TypoPolygon(points: List[TypoPoint])
/** record (via PGObject) */
case class TypoRecord(value: String)
/** regproc (via PGObject) */
case class TypoRegproc(value: String)
/** regtype (via PGObject) */
case class TypoRegtype(value: String)
/** Short primitive */
case class TypoShort(value: Short)
/** xid (via PGObject) */
case class TypoXid(value: String)
/** XML */
case class TypoXml(value: String)
```

## Unknown types

You may also come across column types Typo doesn't know how to handle yet.
You will see warnings in the log at generation-time like this:
```
typo: Couldn't translate type from table pg_catalog.pg_statistic_ext_data column stxdndistinct with type Some(pg_ndistinct). Falling back to text
```

And then you'll see corresponding classes like this:
```scala
/** This is a type typo does not know how to handle yet. This falls back to casting to string and crossing fingers. Time to file an issue! :] */
case class TypoUnknownPgNdistinct(value: String)
```