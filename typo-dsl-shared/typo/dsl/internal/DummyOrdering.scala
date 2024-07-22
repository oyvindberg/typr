package typo.dsl.internal

object DummyOrdering extends Ordering[Any] {
  override def compare(x: Any, y: Any): Int =
    (x, y) match {
      case (x: Comparable[Any] @unchecked, y: Comparable[Any] @unchecked) =>
        x.compareTo(y)
      case _ =>
        x.toString.compareTo(y.toString)
    }

  implicit def ord[T]: Ordering[T] = DummyOrdering.asInstanceOf[Ordering[T]]
}
