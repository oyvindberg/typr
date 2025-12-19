package adventureworks

import adventureworks.DbNow

import adventureworks.humanresources.employee._
import adventureworks.person.businessentity._
import adventureworks.person.emailaddress._
import adventureworks.person.person._
import adventureworks.person_row_join._
import adventureworks.public.Name
import adventureworks.sales.salesperson._
import adventureworks.userdefined.FirstName
import org.junit.Test

import java.time.LocalDate
import java.util.Optional
import scala.jdk.CollectionConverters._

class RecordTest {
  private val personRowJoinSqlRepo = new PersonRowJoinSqlRepoImpl
  private val businessentityRepo = new BusinessentityRepoImpl
  private val personRepo = new PersonRepoImpl
  private val emailaddressRepo = new EmailaddressRepoImpl
  private val employeeRepo = new EmployeeRepoImpl
  private val salespersonRepo = new SalespersonRepoImpl

  @Test
  def works(): Unit = {
    WithConnection {
      val businessentityRow = businessentityRepo.insert(BusinessentityRowUnsaved())

      val personRow = personRepo.insert(
        PersonRowUnsaved(
          businessentityid = businessentityRow.businessentityid,
          persontype = "EM",
          firstname = FirstName("a"),
          lastname = Name("lastname")
        )
      )

      val _ = emailaddressRepo.insert(
        EmailaddressRowUnsaved(businessentityid = personRow.businessentityid)
          .copy(emailaddress = Optional.of("a@b.c"))
      )

      val employeeRow = employeeRepo.insert(
        EmployeeRowUnsaved(
          businessentityid = personRow.businessentityid,
          nationalidnumber = "9912312312",
          loginid = "loginid",
          jobtitle = "jobtitle",
          birthdate = LocalDate.parse("1998-01-01"),
          maritalstatus = "M",
          gender = "M",
          hiredate = LocalDate.parse("1997-01-01")
        )
      )

      val _ = salespersonRepo.insert(SalespersonRowUnsaved(businessentityid = employeeRow.businessentityid))

      personRowJoinSqlRepo.apply.asScala.foreach(println)
    }
  }
}
