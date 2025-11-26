package typo
package internal
package analysis

import play.api.libs.json.{Json, Writes}

case class ParsedName(name: db.ColName, originalName: db.ColName, nullability: Option[Nullability], bareOverriddenType: Option[jvm.Type.Qualified]) {
  val overriddenType: Option[jvm.Type] = bareOverriddenType.map(jvm.Type.UserDefined.apply)
}

object ParsedName {
  def of(name: String): ParsedName = {
    val (shortened, nullability) =
      if (name.endsWith("?")) (name.dropRight(1), Some(Nullability.Nullable))
      else if (name.endsWith("!")) (name.dropRight(1), Some(Nullability.NoNulls))
      else (name, None)

    val (dbName, overriddenType) = shortened.split(":").toList match {
      case Nil                 => sys.error("shouldn't happen (tm)")
      case name :: Nil         => (db.ColName(name), None)
      case name :: tpeStr :: _ => (db.ColName(name), Some(jvm.Type.Qualified(tpeStr)))
    }
    ParsedName(dbName, originalName = db.ColName(name), nullability, overriddenType)
  }

  implicit val oformat: Writes[ParsedName] = (x: ParsedName) =>
    Json.obj(
      "name" -> x.name.value,
      "originalName" -> x.originalName.value,
      "nullability" -> x.nullability.map(_.toString),
      "overriddenType" -> x.bareOverriddenType.map(_.dotName)
    )
}
