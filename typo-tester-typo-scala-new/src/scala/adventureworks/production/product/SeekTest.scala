package adventureworks.production.product

import adventureworks.SnapshotTest
import adventureworks.public.Name
import org.junit.Test
import typo.scaladsl.ScalaDbTypes
import scala.jdk.OptionConverters._

class SeekTest extends SnapshotTest {
  private val productRepo = ProductRepoImpl()

  @Test
  def uniformAscending(): Unit = {
    val query = productRepo.select
      .seek(f => f.name.asc, new typo.dsl.SqlExpr.ConstReq(Name("foo"), Name.pgType))
      .seek(f => f.weight.asc, new typo.dsl.SqlExpr.ConstOpt(Some(BigDecimal("22.2")).toJava, ScalaDbTypes.PgTypes.numeric))
      .seek(f => f.listprice.asc, new typo.dsl.SqlExpr.ConstReq(BigDecimal("33.3"), ScalaDbTypes.PgTypes.numeric))
    compareFragment("uniform-ascending", query.sql())
  }

  @Test
  def uniformDescending(): Unit = {
    val query = productRepo.select
      .seek(f => f.name.desc, new typo.dsl.SqlExpr.ConstReq(Name("foo"), Name.pgType))
      .seek(f => f.weight.desc, new typo.dsl.SqlExpr.ConstOpt(Some(BigDecimal("22.2")).toJava, ScalaDbTypes.PgTypes.numeric))
      .seek(f => f.listprice.desc, new typo.dsl.SqlExpr.ConstReq(BigDecimal("33.3"), ScalaDbTypes.PgTypes.numeric))
    compareFragment("uniform-descending", query.sql())
  }

  @Test
  def complex(): Unit = {
    val query = productRepo.select
      .seek(f => f.name.asc, new typo.dsl.SqlExpr.ConstReq(Name("foo"), Name.pgType))
      .seek(f => f.weight.desc, new typo.dsl.SqlExpr.ConstOpt(Some(BigDecimal("22.2")).toJava, ScalaDbTypes.PgTypes.numeric))
      .seek(f => f.listprice.desc, new typo.dsl.SqlExpr.ConstReq(BigDecimal("33.3"), ScalaDbTypes.PgTypes.numeric))
    compareFragment("complex", query.sql())
  }
}
