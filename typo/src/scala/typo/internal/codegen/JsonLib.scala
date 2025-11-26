package typo
package internal
package codegen

trait JsonLib {
  def defaultedInstance(default: ComputedDefault): List[jvm.Given]
  def stringEnumInstances(wrapperType: jvm.Type, underlying: jvm.Type, openEnum: Boolean): List[jvm.Given]
  def wrapperTypeInstances(wrapperType: jvm.Type.Qualified, fieldName: jvm.Ident, underlying: jvm.Type): List[jvm.Given]
  def productInstances(tpe: jvm.Type, fields: NonEmptyList[JsonLib.Field]): List[jvm.Given]
  def missingInstances: List[jvm.ClassMember]

  final def customTypeInstances(ct: CustomType): List[jvm.Given] =
    ct.params match {
      case NonEmptyList(param, Nil) =>
        wrapperTypeInstances(ct.typoType, param.name, param.tpe)
      case more =>
        productInstances(ct.typoType, more.map(param => JsonLib.Field(param.name, jvm.StrLit(param.name.value), param.tpe)))
    }

  final def instances(tpe: jvm.Type, cols: NonEmptyList[ComputedColumn]): List[jvm.Given] =
    productInstances(tpe, cols.map(col => JsonLib.Field(col.name, jvm.StrLit(col.dbName.value), col.tpe)))
}

object JsonLib {
  case class Field(scalaName: jvm.Ident, jsonName: jvm.StrLit, tpe: jvm.Type)
}
