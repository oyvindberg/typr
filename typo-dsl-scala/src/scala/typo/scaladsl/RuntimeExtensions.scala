package typo.scaladsl

import typo.runtime.*

import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

implicit class PgTypeOps[A](private val pgType: PgType[A]) extends AnyVal {
  def nullable: PgType[Option[A]] =
    pgType.opt.to(Bijections.optionalToOption[A])
}

implicit class MariaTypeOps[A](private val mariaType: MariaType[A]) extends AnyVal {
  def nullable: MariaType[Option[A]] =
    mariaType.opt.to(Bijections.optionalToOption[A])
}

implicit class DbTypeOps[A](private val dbType: DbType[A]) extends AnyVal {
  def nullable: DbType[Option[A]] =
    dbType.opt.to(Bijections.optionalToOption[A])
}

implicit class EitherOps[L, R](private val either: Either[L, R]) extends AnyVal {
  def rightOrNone: Option[R] = {
    either.asOptional().toScala
  }

  def leftOrNone: Option[L] = {
    either match {
      case left: typo.runtime.Either.Left[L, R] => Some(left.value())
      case _                                    => None
    }
  }
}

implicit class ArrOps[A](private val arr: typo.data.Arr[A]) extends AnyVal {
  def reshapeOrNone(newDims: Int*): Option[typo.data.Arr[A]] = {
    arr.reshape(newDims*).toScala
  }

  def getOrNone(indices: Int*): Option[A] = {
    arr.get(indices*).toScala
  }
}

implicit class RangeOps[T <: Comparable[T]](private val range: typo.data.Range[T]) extends AnyVal {
  def finiteOrNone: Option[typo.data.RangeFinite[T]] = {
    range.finite().toScala
  }
}

implicit class FragmentBuilderOps(private val builder: Fragment.Builder) extends AnyVal {
  def paramNullable[T](pgType: PgType[T], value: Option[T]): Fragment.Builder = {
    builder.param(pgType.opt(), value.toJava)
  }
}

implicit class FragmentOps(private val fragment: Fragment) extends AnyVal {
  def query[Out](parser: typo.scaladsl.ResultSetParser[Out]): Operation[Out] = {
    fragment.query(parser.underlying)
  }
}

implicit class OperationOptionalOps[T](private val operation: Operation[java.util.Optional[T]]) extends AnyVal {
  def runUncheckedOrNone(c: java.sql.Connection): Option[T] = {
    operation.runUnchecked(c).toScala
  }
}

// Extension for converting Scala Iterator to Java Iterator for streaming inserts
implicit class ScalaIteratorOps[T](private val iterator: Iterator[T]) extends AnyVal {
  def toJavaIterator: java.util.Iterator[T] = iterator.asJava
}

def buildFragment(block: Fragment.Builder => Unit): Fragment = {
  val builder = Fragment.Builder()
  block(builder)
  builder.done()
}
