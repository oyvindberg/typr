package adventureworks

import adventureworks.customtypes.TypoLocalDate
import adventureworks.person_row_join.PersonRowJoinSqlRepoImpl
import adventureworks.userdefined.FirstName
import org.junit.Test
import kotlin.random.Random

class RecordTest {
    private val personRowJoinSqlRepo = PersonRowJoinSqlRepoImpl()

    @Test
    fun works() {
        WithConnection.run { c ->
            val testInsert = TestInsert(Random(0), DomainInsert)
            val businessentityRow = testInsert.personBusinessentity(c)
            val personRow = testInsert.personPerson(c, businessentityRow.businessentityid, persontype = "EM", firstname = FirstName("a"))
            testInsert.personEmailaddress(c, personRow.businessentityid, emailaddress = "a@b.c")
            val employeeRow = testInsert.humanresourcesEmployee(
                c,
                personRow.businessentityid,
                gender = "M",
                maritalstatus = "M",
                birthdate = TypoLocalDate("1998-01-01"),
                hiredate = TypoLocalDate("1997-01-01")
            )
            testInsert.salesSalesperson(c, employeeRow.businessentityid)
            personRowJoinSqlRepo.apply(c).forEach { println(it) }
        }
    }
}
