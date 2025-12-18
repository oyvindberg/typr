package typo.scaladsl

/** Abstract class wrapper for FieldsExpr to enable Scala code to extend it as a class.
  *
  * In Scala, we want Fields to be an abstract class (not a trait) so that the generated Impl case class can extend it with proper constructor semantics. However, FieldsExpr is a Java interface. This
  * intermediate abstract class bridges the gap.
  */
abstract class FieldsExpr0[Row] extends FieldsExpr[Row] {
  // Inherits all methods from FieldsExpr
  // Generated Fields classes will extend this
}
