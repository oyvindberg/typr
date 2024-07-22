package typo

object TypesScala {
  val Any = sc.Type.Qualified("scala.Any")
  val AnyRef = sc.Type.Qualified("scala.AnyRef")
  val AnyVal = sc.Type.Qualified("scala.AnyVal")
  val Array = sc.Type.Qualified("scala.Array")
  val BigDecimal = sc.Type.Qualified("scala.math.BigDecimal")
  val Boolean = sc.Type.Qualified("scala.Boolean")
  val Byte = sc.Type.Qualified("scala.Byte")
  val Char = sc.Type.Qualified("scala.Char")
  val Double = sc.Type.Qualified("scala.Double")
  val Either = sc.Type.Qualified("scala.Either")
  val Float = sc.Type.Qualified("scala.Float")
  val Function1 = sc.Type.Qualified("scala.Function1")
  val Int = sc.Type.Qualified("scala.Int")
  val Iterable = sc.Type.Qualified("scala.collection.Iterable")
  val Iterator = sc.Type.Qualified("scala.collection.Iterator")
  val Left = sc.Type.Qualified("scala.Left")
  val List = sc.Type.Qualified("scala.List")
  val ListMap = sc.Type.Qualified("scala.collection.immutable.ListMap")
  val Long = sc.Type.Qualified("scala.Long")
  val Map = sc.Type.Qualified("scala.collection.immutable.Map")
  val Nil = sc.Type.Qualified("scala.collection.immutable.Nil")
  val None = sc.Type.Qualified("scala.None")
  val Numeric = sc.Type.Qualified("scala.math.Numeric")
  val Option = sc.Type.Qualified("scala.Option")
  val Random = sc.Type.Qualified("scala.util.Random")
  val Right = sc.Type.Qualified("scala.Right")
  val Short = sc.Type.Qualified("scala.Short")
  val Some = sc.Type.Qualified("scala.Some")
  val StringContext = sc.Type.Qualified("scala.StringContext")
  val Try = sc.Type.Qualified("scala.util.Try")
  val Unit = sc.Type.Qualified("scala.Unit")
  val mutableMap = sc.Type.Qualified("scala.collection.mutable.Map")
  val nowarn = sc.Type.Qualified("scala.annotation.nowarn")

  object Optional {
    def unapply(tpe: sc.Type): Option[sc.Type] = tpe match {
      case sc.Type.ArrayOf(_)                      => scala.None
      case sc.Type.Wildcard                        => scala.None
      case sc.Type.TApply(Option, scala.List(one)) => scala.Some(one)
      case sc.Type.TApply(underlying, _)           => unapply(underlying)
      case sc.Type.Qualified(_)                    => scala.None
      case sc.Type.Abstract(_)                     => scala.None
      case sc.Type.Commented(underlying, _)        => unapply(underlying)
      case sc.Type.ByName(underlying)              => unapply(underlying)
      case sc.Type.UserDefined(underlying)         => unapply(underlying)
    }
  }

  val HasOrdering: Set[sc.Type] =
    Set(
      BigDecimal,
      Boolean,
      Byte,
      Char,
      Double,
      Float,
      Int,
      Long,
      Short,
      TypesJava.BigDecimal,
      TypesJava.Boolean,
      TypesJava.Byte,
      TypesJava.Double,
      TypesJava.Float,
      TypesJava.Instant,
      TypesJava.Integer,
      TypesJava.LocalDate,
      TypesJava.LocalDateTime,
      TypesJava.LocalTime,
      TypesJava.Long,
      TypesJava.OffsetDateTime,
      TypesJava.OffsetTime,
      TypesJava.Short,
      TypesJava.String,
      TypesJava.UUID
    )

  def boxedType(tpe: sc.Type): Option[sc.Type.Qualified] =
    tpe match {
      case Int                              => scala.Some(TypesJava.Integer)
      case Long                             => scala.Some(TypesJava.Long)
      case Float                            => scala.Some(TypesJava.Float)
      case Double                           => scala.Some(TypesJava.Double)
      case Boolean                          => scala.Some(TypesJava.Boolean)
      case Short                            => scala.Some(TypesJava.Short)
      case Byte                             => scala.Some(TypesJava.Byte)
      case Char                             => scala.Some(TypesJava.Character)
      case sc.Type.Commented(underlying, _) => boxedType(underlying)
      case sc.Type.ByName(underlying)       => boxedType(underlying)
      case sc.Type.UserDefined(underlying)  => boxedType(underlying)
      case _                                => scala.None
    }
}
