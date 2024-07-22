package typo
package internal
package codegen

object FileStringEnum {
  def apply(options: InternalOptions, enm: ComputedStringEnum): sc.File = {

    val comments = scaladoc(s"Enum `${enm.dbEnum.name.value}`")(enm.members.toList.map { case (_, v) => " - " + v })

    val members = enm.members.map { case (name, value) =>
      name -> code"case object $name extends ${enm.tpe.name}(${sc.StrLit(value)})"
    }
    val str = sc.Ident("str")

    val instances = List(
      options.dbLib.toList.flatMap(_.stringEnumInstances(enm.tpe, TypesJava.String, enm.dbEnum.name.value, openEnum = false)),
      options.jsonLibs.flatMap(_.stringEnumInstances(enm.tpe, TypesJava.String, openEnum = false))
    ).flatten

    val obj = genObject.withBody(enm.tpe.value, instances)(
      code"""|def apply($str: ${TypesJava.String}): ${TypesScala.Either.of(TypesJava.String, enm.tpe)} =
             |  ByName.get($str).toRight(s"'$$str' does not match any of the following legal values: $$Names")
             |def force($str: String): ${enm.tpe} =
             |  apply($str) match {
             |    case ${TypesScala.Left}(msg) => sys.error(msg)
             |    case ${TypesScala.Right}(value) => value
             |  }
             |${members.map { case (_, definition) => definition }.mkCode("\n")}
             |val All: ${TypesScala.List.of(enm.tpe)} = ${TypesScala.List}(${members.map { case (ident, _) => ident.code }.mkCode(", ")})
             |val Names: ${TypesJava.String} = All.map(_.value).mkString(", ")
             |val ByName: ${TypesScala.Map.of(TypesJava.String, enm.tpe)} = All.map(x => (x.value, x)).toMap
            """.stripMargin
    )
    val body =
      code"""|$comments
             |sealed abstract class ${enm.tpe.name}(val value: ${TypesJava.String})
             |
             |$obj
             |""".stripMargin

    sc.File(enm.tpe, body, secondaryTypes = Nil, scope = Scope.Main)
  }
}
