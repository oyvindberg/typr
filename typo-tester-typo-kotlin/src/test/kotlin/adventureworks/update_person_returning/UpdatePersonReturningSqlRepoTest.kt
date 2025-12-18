package adventureworks.update_person_returning

import adventureworks.WithConnection
import adventureworks.customtypes.TypoLocalDateTime
import org.junit.Test

class UpdatePersonReturningSqlRepoTest {
    private val updatePersonReturningSqlRepo = UpdatePersonReturningSqlRepoImpl()

    @Test
    fun timestampWorks() {
        WithConnection.run { c ->
            updatePersonReturningSqlRepo("1", TypoLocalDateTime.now, c)
        }
    }
}
