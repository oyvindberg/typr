package adventureworks

import adventureworks.JsonEquals.assertJsonEquals
import adventureworks.public.Mydomain
import adventureworks.public.Myenum
import adventureworks.public.pgtest.PgtestRepoImpl
import adventureworks.public.pgtest.PgtestRow
import adventureworks.public.pgtestnull.PgtestnullRepoImpl
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetTime
import java.util.UUID
import org.junit.Test
import org.postgresql.geometric.PGbox
import org.postgresql.geometric.PGcircle
import org.postgresql.geometric.PGline
import org.postgresql.geometric.PGlseg
import org.postgresql.geometric.PGpath
import org.postgresql.geometric.PGpoint
import org.postgresql.geometric.PGpolygon
import org.postgresql.util.PGInterval
import typo.data.Inet
import typo.data.Int2Vector
import typo.data.Json
import typo.data.Jsonb
import typo.data.Money
import typo.data.Vector
import typo.data.Xml

class ArrayTest {
    private val pgtestnullRepo = PgtestnullRepoImpl()
    private val pgtestRepo = PgtestRepoImpl()

    @Test
    fun canInsertPgtestRows() {
        WithConnection.run { c ->
            val before = ArrayTestData.pgTestRow
            val after = pgtestRepo.insert(before, c)
            assertJsonEquals(before, after)
        }
    }

    @Test
    fun canStreamPgtestRows() {
        WithConnection.run { c ->
            val before = listOf(ArrayTestData.pgTestRow)
            pgtestRepo.insertStreaming(before.iterator(), 1, c)
            val after = pgtestRepo.selectAll(c)
            assertJsonEquals(before, after)
        }
    }

    @Test
    fun canInsertNullPgtestnullRows() {
        WithConnection.run { c ->
            val before = ArrayTestData.pgtestnullRow
            val after = pgtestnullRepo.insert(before, c)
            assertJsonEquals(after, before)
        }
    }

    @Test
    fun canInsertNonNullPgtestnullRows() {
        WithConnection.run { c ->
            val before = ArrayTestData.pgtestnullRowWithValues
            val after = pgtestnullRepo.insert(before, c)
            assertJsonEquals(before, after)
        }
    }

    @Test
    fun canStreamPgtestnullRows() {
        WithConnection.run { c ->
            val before = listOf(ArrayTestData.pgtestnullRow, ArrayTestData.pgtestnullRowWithValues)
            pgtestnullRepo.insertStreaming(before.iterator(), 1, c)
            val after = pgtestnullRepo.selectAll(c)
            assertJsonEquals(before, after)
        }
    }

    @Test
    fun canQueryPgtestnullWithDSL() {
        WithConnection.run { c ->
            val row = pgtestnullRepo.insert(ArrayTestData.pgtestnullRowWithValues, c)
            assertJsonEquals(row.bool, pgtestnullRepo.select().where { it.bool().isEqual(row.bool!!) }.toList(c).first().bool)
            assertJsonEquals(row.box, pgtestnullRepo.select().where { it.box().isEqual(row.box!!) }.toList(c).first().box)
            assertJsonEquals(row.bpchar, pgtestnullRepo.select().where { it.bpchar().isEqual(row.bpchar!!) }.toList(c).first().bpchar)
            assertJsonEquals(row.bytea, pgtestnullRepo.select().where { it.bytea().isEqual(row.bytea!!) }.toList(c).first().bytea)
            assertJsonEquals(row.char, pgtestnullRepo.select().where { it.char().isEqual(row.char!!) }.toList(c).first().char)
            assertJsonEquals(row.circle, pgtestnullRepo.select().where { it.circle().isEqual(row.circle!!) }.toList(c).first().circle)
            assertJsonEquals(row.date, pgtestnullRepo.select().where { it.date().isEqual(row.date!!) }.toList(c).first().date)
            assertJsonEquals(row.float4, pgtestnullRepo.select().where { it.float4().isEqual(row.float4!!) }.toList(c).first().float4)
            assertJsonEquals(row.float8, pgtestnullRepo.select().where { it.float8().isEqual(row.float8!!) }.toList(c).first().float8)
            assertJsonEquals(row.hstore, pgtestnullRepo.select().where { it.hstore().isEqual(row.hstore!!) }.toList(c).first().hstore)
            assertJsonEquals(row.inet, pgtestnullRepo.select().where { it.inet().isEqual(row.inet!!) }.toList(c).first().inet)
            assertJsonEquals(row.int2, pgtestnullRepo.select().where { it.int2().isEqual(row.int2!!) }.toList(c).first().int2)
            assertJsonEquals(row.int2vector, pgtestnullRepo.select().where { it.int2vector().isEqual(row.int2vector!!) }.toList(c).first().int2vector)
            assertJsonEquals(row.int4, pgtestnullRepo.select().where { it.int4().isEqual(row.int4!!) }.toList(c).first().int4)
            assertJsonEquals(row.int8, pgtestnullRepo.select().where { it.int8().isEqual(row.int8!!) }.toList(c).first().int8)
            assertJsonEquals(row.interval, pgtestnullRepo.select().where { it.interval().isEqual(row.interval!!) }.toList(c).first().interval)
            // assertJsonEquals(row.json, pgtestnullRepo.select().where { it.json().isEqual(row.json!!) }.toList(c).first().json)
            assertJsonEquals(row.jsonb, pgtestnullRepo.select().where { it.jsonb().isEqual(row.jsonb!!) }.toList(c).first().jsonb)
            assertJsonEquals(row.line, pgtestnullRepo.select().where { it.line().isEqual(row.line!!) }.toList(c).first().line)
            assertJsonEquals(row.lseg, pgtestnullRepo.select().where { it.lseg().isEqual(row.lseg!!) }.toList(c).first().lseg)
            assertJsonEquals(row.money, pgtestnullRepo.select().where { it.money().isEqual(row.money!!) }.toList(c).first().money)
            assertJsonEquals(row.mydomain, pgtestnullRepo.select().where { it.mydomain().isEqual(row.mydomain!!) }.toList(c).first().mydomain)
            // assertJsonEquals(row.myenum, pgtestnullRepo.select().where { it.myenum().isEqual(row.myenum!!) }.toList(c).first().myenum)
            assertJsonEquals(row.name, pgtestnullRepo.select().where { it.name().isEqual(row.name!!) }.toList(c).first().name)
            assertJsonEquals(row.numeric, pgtestnullRepo.select().where { it.numeric().isEqual(row.numeric!!) }.toList(c).first().numeric)
            assertJsonEquals(row.path, pgtestnullRepo.select().where { it.path().isEqual(row.path!!) }.toList(c).first().path)
            // assertJsonEquals(row.point, pgtestnullRepo.select().where { it.point().isEqual(row.point!!) }.toList(c).first().point)
            // assertJsonEquals(row.polygon, pgtestnullRepo.select().where { it.polygon().isEqual(row.polygon!!) }.toList(c).first().polygon)
            assertJsonEquals(row.text, pgtestnullRepo.select().where { it.text().isEqual(row.text!!) }.toList(c).first().text)
            assertJsonEquals(row.time, pgtestnullRepo.select().where { it.time().isEqual(row.time!!) }.toList(c).first().time)
            assertJsonEquals(row.timestamp, pgtestnullRepo.select().where { it.timestamp().isEqual(row.timestamp!!) }.toList(c).first().timestamp)
            assertJsonEquals(row.timestampz, pgtestnullRepo.select().where { it.timestampz().isEqual(row.timestampz!!) }.toList(c).first().timestampz)
            assertJsonEquals(row.timez, pgtestnullRepo.select().where { it.timez().isEqual(row.timez!!) }.toList(c).first().timez)
            assertJsonEquals(row.uuid, pgtestnullRepo.select().where { it.uuid().isEqual(row.uuid!!) }.toList(c).first().uuid)
            assertJsonEquals(row.varchar, pgtestnullRepo.select().where { it.varchar().isEqual(row.varchar!!) }.toList(c).first().varchar)
            assertJsonEquals(row.vector, pgtestnullRepo.select().where { it.vector().isEqual(row.vector!!) }.toList(c).first().vector)
            // assertJsonEquals(row.xml, pgtestnullRepo.select().where { it.xml().isEqual(row.xml) }.toList(c).first().xml)
            // assertJsonEquals(row.boxes, pgtestnullRepo.select().where { it.boxes().isEqual(row.boxes) }.toList(c).first().boxes)
            // assertJsonEquals(row.bpchares, pgtestnullRepo.select().where { it.bpchares().isEqual(row.bpchares) }.toList(c).first().bpchares) // can fix with custom type
            // assertJsonEquals(row.chares, pgtestnullRepo.select().where { it.chares().isEqual(row.chares) }.toList(c).first().chares) // can fix with custom type
            // assertJsonEquals(row.circlees, pgtestnullRepo.select().where { it.circlees().isEqual(row.circlees) }.toList(c).first().circlees)
            assertJsonEquals(row.datees, pgtestnullRepo.select().where { it.datees().isEqual(row.datees!!) }.toList(c).first().datees)
            assertJsonEquals(row.float4es, pgtestnullRepo.select().where { it.float4es().isEqual(row.float4es!!) }.toList(c).first().float4es)
            assertJsonEquals(row.float8es, pgtestnullRepo.select().where { it.float8es().isEqual(row.float8es!!) }.toList(c).first().float8es)
            assertJsonEquals(row.inetes, pgtestnullRepo.select().where { it.inetes().isEqual(row.inetes!!) }.toList(c).first().inetes)
            assertJsonEquals(row.int2es, pgtestnullRepo.select().where { it.int2es().isEqual(row.int2es!!) }.toList(c).first().int2es)
            assertJsonEquals(row.int2vectores, pgtestnullRepo.select().where { it.int2vectores().isEqual(row.int2vectores!!) }.toList(c).first().int2vectores)
            assertJsonEquals(row.int4es, pgtestnullRepo.select().where { it.int4es().isEqual(row.int4es!!) }.toList(c).first().int4es)
            // assertJsonEquals(row.int8es, pgtestnullRepo.select().where { it.int8es().isEqual(row.int8es!!) }.toList(c).first().int8es)
            assertJsonEquals(row.intervales, pgtestnullRepo.select().where { it.intervales().isEqual(row.intervales!!) }.toList(c).first().intervales)
            // assertJsonEquals(row.jsones, pgtestnullRepo.select().where { it.jsones().isEqual(row.jsones!!) }.toList(c).first().jsones)
            // assertJsonEquals(row.jsonbes, pgtestnullRepo.select().where { it.jsonbes().isEqual(row.jsonbes!!) }.toList(c).first().jsonbes)
            // assertJsonEquals(row.linees, pgtestnullRepo.select().where { it.linees().isEqual(row.linees!!) }.toList(c).first().linees)
            // assertJsonEquals(row.lseges, pgtestnullRepo.select().where { it.lseges().isEqual(row.lseges!!) }.toList(c).first().lseges)
            assertJsonEquals(row.moneyes, pgtestnullRepo.select().where { it.moneyes().isEqual(row.moneyes!!) }.toList(c).first().moneyes)
            assertJsonEquals(row.mydomaines, pgtestnullRepo.select().where { it.mydomaines().isEqual(row.mydomaines!!) }.toList(c).first().mydomaines)
            assertJsonEquals(row.myenumes, pgtestnullRepo.select().where { it.myenumes().isEqual(row.myenumes!!) }.toList(c).first().myenumes)
            // assertJsonEquals(row.namees, pgtestnullRepo.select().where { it.namees().isEqual(row.namees!!) }.toList(c).first().namees)
            // assertJsonEquals(row.numerices, pgtestnullRepo.select().where { it.numerices().isEqual(row.numerices!!) }.toList(c).first().numerices)
            // assertJsonEquals(row.pathes, pgtestnullRepo.select().where { it.pathes().isEqual(row.pathes!!) }.toList(c).first().pathes)
            // assertJsonEquals(row.pointes, pgtestnullRepo.select().where { it.pointes().isEqual(row.pointes!!) }.toList(c).first().pointes)
            // assertJsonEquals(row.polygones, pgtestnullRepo.select().where { it.polygones().isEqual(row.polygones!!) }.toList(c).first().polygones)
            assertJsonEquals(row.textes, pgtestnullRepo.select().where { it.textes().isEqual(row.textes!!) }.toList(c).first().textes)
            assertJsonEquals(row.timees, pgtestnullRepo.select().where { it.timees().isEqual(row.timees!!) }.toList(c).first().timees)
            assertJsonEquals(row.timestampes, pgtestnullRepo.select().where { it.timestampes().isEqual(row.timestampes!!) }.toList(c).first().timestampes)
            assertJsonEquals(row.timestampzes, pgtestnullRepo.select().where { it.timestampzes().isEqual(row.timestampzes!!) }.toList(c).first().timestampzes)
            assertJsonEquals(row.timezes, pgtestnullRepo.select().where { it.timezes().isEqual(row.timezes!!) }.toList(c).first().timezes)
            assertJsonEquals(row.uuides, pgtestnullRepo.select().where { it.uuides().isEqual(row.uuides!!) }.toList(c).first().uuides)
            // assertJsonEquals(row.varchares, pgtestnullRepo.select().where { it.varchares().isEqual(row.varchares) }.toList(c).first().varchares)
            // assertJsonEquals(row.xmles, pgtestnullRepo.select().where { it.xmles().isEqual(row.xmles) }.toList(c).first().xmles)
        }
    }

    @Test
    fun canQueryPgtestWithDSL() {
        WithConnection.run { c ->
            val row = pgtestRepo.insert(ArrayTestData.pgTestRow, c)
            pgtestRepo.update().setValue({ it.bool().underlying }, row.bool).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.box().underlying }, row.box).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.bpchar().underlying }, row.bpchar).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.bytea().underlying }, row.bytea).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.char().underlying }, row.char).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.circle().underlying }, row.circle).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.date().underlying }, row.date).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.float4().underlying }, row.float4).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.float8().underlying }, row.float8).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.hstore().underlying }, row.hstore).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.inet().underlying }, row.inet).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.int2().underlying }, row.int2).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.int2vector().underlying }, row.int2vector).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.int4().underlying }, row.int4).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.int8().underlying }, row.int8).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.interval().underlying }, row.interval).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.json().underlying }, row.json).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.jsonb().underlying }, row.jsonb).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.line().underlying }, row.line).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.lseg().underlying }, row.lseg).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.money().underlying }, row.money).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.mydomain().underlying }, row.mydomain).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.myenum().underlying }, row.myenum).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.name().underlying }, row.name).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.numeric().underlying }, row.numeric).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.path().underlying }, row.path).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.point().underlying }, row.point).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.polygon().underlying }, row.polygon).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.text().underlying }, row.text).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.time().underlying }, row.time).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.timestamp().underlying }, row.timestamp).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.timestampz().underlying }, row.timestampz).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.timez().underlying }, row.timez).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.uuid().underlying }, row.uuid).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.varchar().underlying }, row.varchar).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.vector().underlying }, row.vector).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.xml().underlying }, row.xml).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.boxes().underlying }, row.boxes).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.bpchares().underlying }, row.bpchares).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.chares().underlying }, row.chares).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.circlees().underlying }, row.circlees).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.datees().underlying }, row.datees).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.float4es().underlying }, row.float4es).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.float8es().underlying }, row.float8es).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.inetes().underlying }, row.inetes).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.int2es().underlying }, row.int2es).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.int2vectores().underlying }, row.int2vectores).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.int4es().underlying }, row.int4es).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.int8es().underlying }, row.int8es).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.intervales().underlying }, row.intervales).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.jsones().underlying }, row.jsones).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.jsonbes().underlying }, row.jsonbes).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.linees().underlying }, row.linees).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.lseges().underlying }, row.lseges).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.moneyes().underlying }, row.moneyes).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.mydomaines().underlying }, row.mydomaines).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.myenumes().underlying }, row.myenumes).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.namees().underlying }, row.namees).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.numerices().underlying }, row.numerices).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.pathes().underlying }, row.pathes).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.pointes().underlying }, row.pointes).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.polygones().underlying }, row.polygones).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.textes().underlying }, row.textes).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.timees().underlying }, row.timees).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.timestampes().underlying }, row.timestampes).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.timestampzes().underlying }, row.timestampzes).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.timezes().underlying }, row.timezes).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.uuides().underlying }, row.uuides).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.varchares().underlying }, row.varchares).where { it.uuid().isEqual(row.uuid) }.execute(c)
            pgtestRepo.update().setValue({ it.xmles().underlying }, row.xmles).where { it.uuid().isEqual(row.uuid) }.execute(c)
        }
    }
}
