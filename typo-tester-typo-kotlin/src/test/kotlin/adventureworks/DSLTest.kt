package adventureworks

import adventureworks.customtypes.TypoLocalDate
import adventureworks.humanresources.employee.EmployeeRepoImpl
import adventureworks.person.businessentity.BusinessentityRepoImpl
import adventureworks.person.emailaddress.EmailaddressRepoImpl
import adventureworks.person.person.PersonRepoImpl
import adventureworks.sales.salesperson.SalespersonRepoImpl
import adventureworks.userdefined.FirstName
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.random.Random

class DSLTest {
    private val businessentityRepoImpl = BusinessentityRepoImpl()
    private val personRepoImpl = PersonRepoImpl()
    private val employeeRepoImpl = EmployeeRepoImpl()
    private val salespersonRepoImpl = SalespersonRepoImpl()
    private val emailaddressRepoImpl = EmailaddressRepoImpl()

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
            val salespersonRow = testInsert.salesSalesperson(c, employeeRow.businessentityid)

            val q = salespersonRepoImpl.select()
                .where { it.rowguid().isEqual(salespersonRow.rowguid) }
                .joinFk({ it.fkHumanresourcesEmployee() }, employeeRepoImpl.select())
                .joinFk({ it._2().fkPersonPerson() }, personRepoImpl.select())
                .joinFk({ it._2().fkBusinessentity() }, businessentityRepoImpl.select())
                .join(emailaddressRepoImpl.select().orderBy { it.rowguid().asc() })
                .on { sp_e_p_b_email -> sp_e_p_b_email._2().businessentityid().isEqual(sp_e_p_b_email._1()._2().businessentityid()) }
                .joinOn(salespersonRepoImpl.select()) { sp_e_p_b_email_s2 ->
                    sp_e_p_b_email_s2._1()._1()._1()._2().businessentityid().underlying.isEqual(sp_e_p_b_email_s2._2().businessentityid().underlying)
                }

            val doubled = q.join(q).on { left_right ->
                left_right._1()._1()._1()._1()._2().businessentityid().isEqual(left_right._2()._1()._1()._1()._2().businessentityid())
            }

            doubled.toList(c).forEach { println(it) }
            val count = doubled.count(c)
            assertEquals(1, count.toInt())

            SnapshotTest.compareFragment("DSLTest", "doubled", doubled.sql())
        }
    }
}
