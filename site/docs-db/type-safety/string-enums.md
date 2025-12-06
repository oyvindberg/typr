---
title: String enums
---

If you use string enums in PostgreSQL like this:

```postgresql
CREATE TYPE sector AS ENUM ('PUBLIC', 'PRIVATE', 'OTHER');
```

Typo will generate a type for it with the corresponding type class instances:

Note that like all generated code from Typo it is in the scala 2/3 shared subset, so
it doesn't use `enum` for now.

```scala
/** Enum `myschema.sector`
 *  - PUBLIC
 *  - PRIVATE
 *  - OTHER
 */
sealed abstract class Sector(val value: String)

object Sector {
  def apply(str: String): Either[String, Sector] =
    ByName.get(str).toRight(s"'$str' does not match any of the following legal values: $Names")
  def force(str: String): Sector =
    apply(str) match {
      case Left(msg) => sys.error(msg)
      case Right(value) => value
    }
  case object PUBLIC extends Sector("PUBLIC")
  case object PRIVATE extends Sector("PRIVATE")
  case object OTHER extends Sector("OTHER")
  val All: List[Sector] = List(PUBLIC, PRIVATE, OTHER)
  val Names: String = All.map(_.value).mkString(", ")
  val ByName: Map[String, Sector] = All.map(x => (x.value, x)).toMap

  // ...instances
}
```