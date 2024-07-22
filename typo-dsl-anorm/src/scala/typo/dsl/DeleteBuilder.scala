package typo.dsl

import anorm.{RowParser, SQL, SimpleSql}
import typo.dsl.Fragment.FragmentStringInterpolator

import java.sql.Connection
import java.util.concurrent.atomic.AtomicInteger
import scala.annotation.nowarn

trait DeleteBuilder[Fields, Row] {
  protected def params: DeleteParams[Fields]
  protected def withParams(sqlParams: DeleteParams[Fields]): DeleteBuilder[Fields, Row]

  final def where(v: Fields => SqlExpr[Boolean]): DeleteBuilder[Fields, Row] =
    whereStrict(f => v(f).coalesce(false))

  final def whereStrict(v: Fields => SqlExpr[Boolean]): DeleteBuilder[Fields, Row] =
    withParams(params.where(v))

  def sql: Option[Fragment]
  def execute()(implicit c: Connection): Int
}

object DeleteBuilder {
  def apply[Fields, Row](name: String, structure: Structure.Relation[Fields, Row]): DeleteBuilderSql[Fields, Row] =
    DeleteBuilderSql(name, structure, DeleteParams.empty)

  final case class DeleteBuilderSql[Fields, Row](
      name: String,
      structure: Structure.Relation[Fields, Row],
      params: DeleteParams[Fields]
  ) extends DeleteBuilder[Fields, Row] {
    override def withParams(sqlParams: DeleteParams[Fields]): DeleteBuilder[Fields, Row] =
      copy(params = sqlParams)

    def mkSql(ctx: RenderCtx): Fragment = {
      List[Iterable[Fragment]](
        Some(frag"delete from ${Fragment(name)}"),
        params.where
          .map(w => w(structure.fields))
          .reduceLeftOption(_.and(_))
          .map { where => Fragment(" where ") ++ where.render(ctx, new AtomicInteger(0)) }
      ).flatten.reduce(_ ++ _)
    }

    override def sql: Option[Fragment] =
      Some(mkSql(RenderCtx.Empty))

    override def execute()(implicit c: Connection): Int = {
      val frag = mkSql(RenderCtx.Empty)
      SimpleSql(SQL(frag.sql), frag.params.map(_.tupled).toMap, RowParser.successful).executeUpdate()
    }
  }

  final case class DeleteBuilderMock[Id, Fields, Row](
      params: DeleteParams[Fields],
      structure: Structure[Fields, Row],
      map: scala.collection.mutable.Map[Id, Row]
  ) extends DeleteBuilder[Fields, Row] {
    override def withParams(sqlParams: DeleteParams[Fields]): DeleteBuilder[Fields, Row] =
      copy(params = sqlParams)

    override def sql: Option[Fragment] =
      None

    override def execute()(implicit @nowarn c: Connection): Int = {
      var changed: Int = 0
      map.foreach { case (id, row) =>
        if (params.where.forall(w => structure.untypedEval(w(structure.fields), row).getOrElse(false))) {
          map.remove(id): @nowarn
          changed += 1
        }
      }
      changed
    }
  }
}
