package testdb

import org.junit.Assert._
import org.junit.Test

import scala.util.Random

/** Tests for TestInsert functionality - automatic random data generation for testing.
  */
class TestInsertTest {
  private val testInsert = TestInsert(Random(42))

  @Test
  def testCustomersInsert(): Unit = withConnection { c =>
    given java.sql.Connection = c

    val row = testInsert.Customers()
    assertNotNull(row)
    assertNotNull(row.customerId)
    assertNotNull(row.name)
  }

  @Test
  def testCustomersWithCustomization(): Unit = withConnection { c =>
    given java.sql.Connection = c

    val row = testInsert.Customers(name = "Custom Name")

    assertNotNull(row)
    assertEquals("Custom Name", row.name)
  }

  @Test
  def testDepartmentsInsert(): Unit = withConnection { c =>
    given java.sql.Connection = c

    val row = testInsert.Departments()

    assertNotNull(row)
    assertNotNull(row.deptCode)
    assertNotNull(row.deptRegion)
    assertNotNull(row.deptName)
  }

  @Test
  def testProductsInsert(): Unit = withConnection { c =>
    given java.sql.Connection = c

    val row = testInsert.Products()

    assertNotNull(row)
    assertNotNull(row.productId)
    assertNotNull(row.sku)
    assertNotNull(row.name)
    assertNotNull(row.price)
  }

  @Test
  def testAllScalarTypesInsert(): Unit = withConnection { c =>
    given java.sql.Connection = c

    val row = testInsert.AllScalarTypes()

    assertNotNull(row)
    assertNotNull(row.id)
    assertNotNull(row.colNotNull)
  }

  @Test
  def testEmployeesWithDepartmentFK(): Unit = withConnection { c =>
    given java.sql.Connection = c

    val dept = testInsert.Departments()

    val emp = testInsert.Employees(deptCode = dept.deptCode, deptRegion = dept.deptRegion)

    assertNotNull(emp)
    assertEquals(dept.deptCode, emp.deptCode)
    assertEquals(dept.deptRegion, emp.deptRegion)
  }

  @Test
  def testOrdersWithCustomerFK(): Unit = withConnection { c =>
    given java.sql.Connection = c

    val customer = testInsert.Customers()

    val order = testInsert.Orders(customerId = customer.customerId.value)

    assertNotNull(order)
    assertEquals(customer.customerId.value, order.customerId)
  }

  @Test
  def testMultipleInserts(): Unit = withConnection { c =>
    given java.sql.Connection = c

    val row1 = testInsert.Customers()
    val row2 = testInsert.Customers()
    val row3 = testInsert.Customers()

    assertNotEquals(row1.customerId, row2.customerId)
    assertNotEquals(row2.customerId, row3.customerId)
    assertNotEquals(row1.customerId, row3.customerId)
  }

  @Test
  def testInsertWithSeededRandom(): Unit = {
    val testInsert1 = TestInsert(Random(123))
    val testInsert2 = TestInsert(Random(123))

    val row1 = withConnection { c =>
      given java.sql.Connection = c
      testInsert1.Customers()
    }
    val row2 = withConnection { c =>
      given java.sql.Connection = c
      testInsert2.Customers()
    }

    assertEquals(row1.name, row2.name)
  }

  @Test
  def testDepartmentsGeneratesValidData(): Unit = withConnection { c =>
    given java.sql.Connection = c

    val row = testInsert.Departments()

    assertNotNull(row)
    assertNotNull(row.deptCode)
    assertNotNull(row.deptRegion)
    assertNotNull(row.deptName)
  }
}
