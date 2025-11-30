package typo
package internal
package codegen

object IocAnnotations {
  def forMainScope(iocFramework: Option[IocFramework]): List[jvm.Annotation] =
    iocFramework match {
      case Some(IocFramework.Spring) =>
        List(jvm.Annotation(TypesJava.spring.Repository))
      case Some(IocFramework.JakartaCdi) =>
        List(jvm.Annotation(TypesJava.jakarta.ApplicationScoped))
      case None =>
        Nil
    }

  def forTestScope(iocFramework: Option[IocFramework]): List[jvm.Annotation] =
    iocFramework match {
      case Some(IocFramework.Spring) =>
        List(jvm.Annotation(TypesJava.spring.Repository))
      case Some(IocFramework.JakartaCdi) =>
        List(jvm.Annotation(TypesJava.jakarta.Singleton))
      case None =>
        Nil
    }
}
