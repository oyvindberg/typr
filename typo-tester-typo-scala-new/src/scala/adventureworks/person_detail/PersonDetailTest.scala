package adventureworks.person_detail

import adventureworks.DbNow

import adventureworks.WithConnection
import adventureworks.person.businessentity.BusinessentityId
import org.junit.Test

import java.time.LocalDateTime

class PersonDetailTest {
  private val personDetailSqlRepo = new PersonDetailSqlRepoImpl

  @Test
  def timestampWorks(): Unit = {
    WithConnection {
      val _ = personDetailSqlRepo.apply(BusinessentityId(1), DbNow.localDateTime())
    }
  }
}
