package testdb

import org.junit.Assert.*
import org.junit.Test
import testdb.departments.*
import testdb.employees.*
import java.math.BigDecimal
import java.time.LocalDate

class CompositeKeyTest {
    private val departmentsRepo = DepartmentsRepoImpl()
    private val employeesRepo = EmployeesRepoImpl()

    @Test
    fun testDepartmentsInsert() {
        DuckDbTestHelper.run { c ->
            val unique = System.nanoTime().toString().takeLast(6)
            val dept = DepartmentsRow("IT$unique", "US-WEST", "Information Technology", BigDecimal("1000000"))

            val inserted = departmentsRepo.insert(dept, c)

            assertNotNull(inserted)
            assertEquals("IT$unique", inserted.deptCode)
            assertEquals("US-WEST", inserted.deptRegion)
            assertEquals("Information Technology", inserted.deptName)
        }
    }

    @Test
    fun testDepartmentsSelectByCompositeId() {
        DuckDbTestHelper.run { c ->
            val unique = System.nanoTime().toString().takeLast(6)
            val dept = DepartmentsRow("HR$unique", "EU-EAST", "Human Resources", null)
            departmentsRepo.insert(dept, c)

            val id = DepartmentsId("HR$unique", "EU-EAST")
            val found = departmentsRepo.selectById(id, c)

            assertNotNull(found)
            assertEquals("HR$unique", found!!.deptCode)
            assertEquals("EU-EAST", found.deptRegion)
        }
    }

    @Test
    fun testDepartmentsCompositeIdFromRow() {
        DuckDbTestHelper.run { c ->
            val unique = System.nanoTime().toString().takeLast(6)
            val dept = DepartmentsRow("SALES$unique", "APAC", "Sales APAC", BigDecimal("500000"))
            val inserted = departmentsRepo.insert(dept, c)

            val compositeId = inserted.compositeId()
            assertEquals("SALES$unique", compositeId.deptCode)
            assertEquals("APAC", compositeId.deptRegion)

            val found = departmentsRepo.selectById(compositeId, c)
            assertNotNull(found)
            assertEquals(inserted, found!!)
        }
    }

    @Test
    fun testDepartmentsUpdate() {
        DuckDbTestHelper.run { c ->
            val unique = System.nanoTime().toString().takeLast(6)
            val dept = DepartmentsRow("FIN$unique", "US-CENTRAL", "Finance", BigDecimal("800000"))
            val inserted = departmentsRepo.insert(dept, c)

            val updated = inserted.copy(budget = BigDecimal("900000"))
            val wasUpdated = departmentsRepo.update(updated, c)
            assertTrue(wasUpdated)

            val found = departmentsRepo.selectById(inserted.compositeId(), c)!!
            assertEquals(0, BigDecimal("900000").compareTo(found.budget))
        }
    }

    @Test
    fun testDepartmentsDelete() {
        DuckDbTestHelper.run { c ->
            val unique = System.nanoTime().toString().takeLast(6)
            val dept = DepartmentsRow("TMP$unique", "TEMP-REGION", "Temporary", null)
            val inserted = departmentsRepo.insert(dept, c)

            val deleted = departmentsRepo.deleteById(inserted.compositeId(), c)
            assertTrue(deleted)

            val found = departmentsRepo.selectById(inserted.compositeId(), c)
            assertNull(found)
        }
    }

    @Test
    fun testEmployeesInsert() {
        DuckDbTestHelper.run { c ->
            val unique = System.nanoTime().toString().takeLast(6)
            val empNum = (System.nanoTime() % 100000).toInt()
            departmentsRepo.insert(DepartmentsRow("DEV$unique", "US", "Development", null), c)

            val emp = EmployeesRow(
                empNum, "A", "DEV$unique", "US", "Alice Johnson",
                BigDecimal("95000"), LocalDate.of(2025, 1, 15)
            )

            val inserted = employeesRepo.insert(emp, c)

            assertNotNull(inserted)
            assertEquals(empNum, inserted.empNumber)
            assertEquals("A", inserted.empSuffix)
            assertEquals("Alice Johnson", inserted.empName)
        }
    }

    @Test
    fun testEmployeesSelectByCompositeId() {
        DuckDbTestHelper.run { c ->
            val unique = System.nanoTime().toString().takeLast(6)
            val empNum = (System.nanoTime() % 100000).toInt()
            departmentsRepo.insert(DepartmentsRow("QA$unique", "EU", "Quality Assurance", null), c)

            val emp = EmployeesRow(empNum, "B", "QA$unique", "EU", "Bob Smith", BigDecimal("75000"), LocalDate.of(2024, 6, 1))
            employeesRepo.insert(emp, c)

            val id = EmployeesId(empNum, "B")
            val found = employeesRepo.selectById(id, c)

            assertNotNull(found)
            assertEquals(empNum, found!!.empNumber)
            assertEquals("B", found.empSuffix)
        }
    }

    @Test
    fun testEmployeeDepartmentForeignKey() {
        DuckDbTestHelper.run { c ->
            val unique = System.nanoTime().toString().takeLast(6)
            val empNum = (System.nanoTime() % 100000).toInt()
            val dept = DepartmentsRow("FK$unique", "FK_REGION", "FK Test Dept", BigDecimal("500000"))
            departmentsRepo.insert(dept, c)

            val emp = EmployeesRow(empNum, "F", "FK$unique", "FK_REGION", "FK Employee", BigDecimal("60000"), LocalDate.now())
            val insertedEmp = employeesRepo.insert(emp, c)

            assertEquals(dept.deptCode, insertedEmp.deptCode)
            assertEquals(dept.deptRegion, insertedEmp.deptRegion)

            val foundDept = departmentsRepo.selectById(DepartmentsId(insertedEmp.deptCode, insertedEmp.deptRegion), c)
            assertNotNull(foundDept)
            assertEquals("FK Test Dept", foundDept!!.deptName)
        }
    }
}
