package typo
package internal
package codegen

object FileCustomType {
  def apply(options: InternalOptions, lang: Lang)(ct: CustomType): jvm.File = {

    val maybeBijection = ct.params match {
      case NonEmptyList(jvm.Param(_, name, underlying, _), Nil) if options.enableDsl =>
        val bijection = {
          val thisBijection = jvm.Type.dsl.Bijection.of(ct.typoType, underlying)
          val expr = lang.bijection(ct.typoType, underlying, jvm.FieldGetterRef(ct.typoType, name), jvm.ConstructorMethodRef(ct.typoType))
          jvm.Given(Nil, jvm.Ident("bijection"), Nil, thisBijection, expr)
        }
        Some(bijection)
      case _ => None
    }

    val instances =
      maybeBijection.toList ++
        options.jsonLibs.flatMap(_.customTypeInstances(ct)) ++
        options.dbLib.toList.flatMap(_.customTypeInstances(ct))

    val cls = jvm.Adt.Record(
      isWrapper = false,
      comments = scaladoc(List(ct.comment)),
      name = ct.typoType,
      tparams = Nil,
      params = ct.params.toList,
      implicitParams = Nil,
      `extends` = None,
      implements = Nil,
      members = Nil,
      staticMembers = instances ++ ct.objBody0
    )

    jvm.File(ct.typoType, cls, secondaryTypes = Nil, scope = Scope.Main)
  }
}
