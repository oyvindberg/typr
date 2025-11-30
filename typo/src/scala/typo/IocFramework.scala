package typo

sealed trait IocFramework

object IocFramework {
  case object Spring extends IocFramework
  case object JakartaCdi extends IocFramework
}
