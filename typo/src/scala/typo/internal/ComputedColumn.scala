package typo
package internal

case class ComputedColumn(
    pointsTo: List[(Source.Relation, db.ColName)],
    name: jvm.Ident,
    tpe: jvm.Type,
    dbCol: db.Col
) {
  def dbName = dbCol.name
  def param: jvm.Param[jvm.Type] = jvm.Param(name, tpe)
}
