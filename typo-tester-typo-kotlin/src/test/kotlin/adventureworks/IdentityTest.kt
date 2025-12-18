package adventureworks

import adventureworks.customtypes.Defaulted
import adventureworks.public_.identity_test.*
import org.junit.Assert.assertEquals
import org.junit.Test

class IdentityTest {
    private val repo = IdentityTestRepoImpl()

    @Test
    fun works() {
        WithConnection.run { c ->
            val unsaved = IdentityTestRowUnsaved(IdentityTestId("a"), Defaulted.UseDefault)
            val inserted = repo.insert(unsaved, c)
            val upserted = repo.upsert(inserted, c)
            assertEquals(inserted, upserted)

            val result = repo.select()
                .orderBy { it.name().asc() }
                .toList(c)

            assertEquals(
                listOf(IdentityTestRow(1, 1, IdentityTestId("a"))),
                result
            )
        }
    }
}
