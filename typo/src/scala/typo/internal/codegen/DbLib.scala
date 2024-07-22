package typo
package internal
package codegen

trait DbLib {
  def resolveConstAs(tpe: sc.Type): sc.Code
  def defaultedInstance: List[sc.Given]
  def repoSig(repoMethod: RepoMethod): Either[DbLib.NotImplementedFor, sc.Code]
  def repoImpl(repoMethod: RepoMethod): sc.Code
  def mockRepoImpl(id: IdComputed, repoMethod: RepoMethod, maybeToRow: Option[sc.Param]): sc.Code
  def testInsertMethod(x: ComputedTestInserts.InsertMethod): sc.Value
  def stringEnumInstances(wrapperType: sc.Type, underlying: sc.Type, sqlType: String, openEnum: Boolean): List[sc.ClassMember]
  def wrapperTypeInstances(wrapperType: sc.Type.Qualified, underlying: sc.Type, overrideDbType: Option[String]): List[sc.ClassMember]
  def missingInstances: List[sc.ClassMember]
  def rowInstances(tpe: sc.Type, cols: NonEmptyList[ComputedColumn], rowType: DbLib.RowType): List[sc.ClassMember]
  def customTypeInstances(ct: CustomType): List[sc.ClassMember]
  def additionalFiles: List[sc.File]
}

object DbLib {
  case class NotImplementedFor(library: String)

  sealed trait RowType
  object RowType {
    case object Readable extends RowType
    case object Writable extends RowType
    case object ReadWriteable extends RowType
  }
}
