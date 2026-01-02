package testdb;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.Test;
import testdb.departments.*;
import testdb.employees.*;

/**
 * Tests for composite primary keys in DuckDB. Tests the departments (2-column String,String) and
 * employees (2-column Integer,String) tables with composite PKs.
 */
public class CompositeKeyTest {
  private final DepartmentsRepoImpl departmentsRepo = new DepartmentsRepoImpl();
  private final EmployeesRepoImpl employeesRepo = new EmployeesRepoImpl();

  // ==================== Departments (String, String) Composite Key ====================

  @Test
  public void testDepartmentsInsert() {
    DuckDbTestHelper.run(
        c -> {
          var unique = String.valueOf(System.nanoTime());
          var dept =
              new DepartmentsRow(
                  "IT" + unique,
                  "US-WEST",
                  "Information Technology",
                  Optional.of(new BigDecimal("1000000")));

          var inserted = departmentsRepo.insert(dept, c);

          assertNotNull(inserted);
          assertEquals("IT" + unique, inserted.deptCode());
          assertEquals("US-WEST", inserted.deptRegion());
          assertEquals("Information Technology", inserted.deptName());
          assertTrue(inserted.budget().isPresent());
          assertEquals(0, inserted.budget().get().compareTo(new BigDecimal("1000000")));
        });
  }

  @Test
  public void testDepartmentsSelectByCompositeId() {
    DuckDbTestHelper.run(
        c -> {
          var dept = new DepartmentsRow("HR", "EU-EAST", "Human Resources", Optional.empty());
          departmentsRepo.insert(dept, c);

          // Select by composite ID
          var id = new DepartmentsId("HR", "EU-EAST");
          var found = departmentsRepo.selectById(id, c);

          assertTrue(found.isPresent());
          assertEquals("HR", found.get().deptCode());
          assertEquals("EU-EAST", found.get().deptRegion());
        });
  }

  @Test
  public void testDepartmentsCompositeIdFromRow() {
    DuckDbTestHelper.run(
        c -> {
          var dept =
              new DepartmentsRow(
                  "SALES", "APAC", "Sales APAC", Optional.of(new BigDecimal("500000")));
          var inserted = departmentsRepo.insert(dept, c);

          // Get composite ID from row
          var compositeId = inserted.compositeId();
          assertEquals("SALES", compositeId.deptCode());
          assertEquals("APAC", compositeId.deptRegion());

          // Use it to select
          var found = departmentsRepo.selectById(compositeId, c);
          assertTrue(found.isPresent());
          assertEquals(inserted, found.get());
        });
  }

  @Test
  public void testDepartmentsUpdate() {
    DuckDbTestHelper.run(
        c -> {
          var dept =
              new DepartmentsRow(
                  "FINANCE", "US-CENTRAL", "Finance", Optional.of(new BigDecimal("800000")));
          var inserted = departmentsRepo.insert(dept, c);

          // Update budget
          var updated = inserted.withBudget(Optional.of(new BigDecimal("900000")));
          boolean wasUpdated = departmentsRepo.update(updated, c);
          assertTrue(wasUpdated);

          var found = departmentsRepo.selectById(inserted.compositeId(), c).orElseThrow();
          assertEquals(Optional.of(new BigDecimal("900000.00")), found.budget());
        });
  }

  @Test
  public void testDepartmentsDelete() {
    DuckDbTestHelper.run(
        c -> {
          var dept = new DepartmentsRow("TEMP", "TEMP-REGION", "Temporary", Optional.empty());
          var inserted = departmentsRepo.insert(dept, c);

          boolean deleted = departmentsRepo.deleteById(inserted.compositeId(), c);
          assertTrue(deleted);

          var found = departmentsRepo.selectById(inserted.compositeId(), c);
          assertFalse(found.isPresent());
        });
  }

  @Test
  public void testDepartmentsMultipleSameCode() {
    DuckDbTestHelper.run(
        c -> {
          // Insert departments with same code but different regions
          var dept1 = new DepartmentsRow("ENG", "US", "Engineering US", Optional.empty());
          var dept2 = new DepartmentsRow("ENG", "EU", "Engineering EU", Optional.empty());
          var dept3 = new DepartmentsRow("ENG", "APAC", "Engineering APAC", Optional.empty());

          departmentsRepo.insert(dept1, c);
          departmentsRepo.insert(dept2, c);
          departmentsRepo.insert(dept3, c);

          // Each should be independently retrievable
          assertTrue(departmentsRepo.selectById(new DepartmentsId("ENG", "US"), c).isPresent());
          assertTrue(departmentsRepo.selectById(new DepartmentsId("ENG", "EU"), c).isPresent());
          assertTrue(departmentsRepo.selectById(new DepartmentsId("ENG", "APAC"), c).isPresent());
        });
  }

  // ==================== Employees (Integer, String) Composite Key ====================

  @Test
  public void testEmployeesInsert() {
    DuckDbTestHelper.run(
        c -> {
          var unique = String.valueOf(System.nanoTime());
          // First create a department (FK constraint)
          var dept = new DepartmentsRow("DEV" + unique, "US", "Development", Optional.empty());
          departmentsRepo.insert(dept, c);

          // Now create employee
          var empNum = (int) (System.nanoTime() % 1000000);
          var emp =
              new EmployeesRow(
                  empNum,
                  "A",
                  "DEV" + unique,
                  "US",
                  "Alice Johnson",
                  Optional.of(new BigDecimal("95000")),
                  LocalDate.of(2025, 1, 15));

          var inserted = employeesRepo.insert(emp, c);

          assertNotNull(inserted);
          assertEquals(Integer.valueOf(empNum), inserted.empNumber());
          assertEquals("A", inserted.empSuffix());
          assertEquals("Alice Johnson", inserted.empName());
        });
  }

  @Test
  public void testEmployeesSelectByCompositeId() {
    DuckDbTestHelper.run(
        c -> {
          // Create department first
          departmentsRepo.insert(
              new DepartmentsRow("QA", "EU", "Quality Assurance", Optional.empty()), c);

          // Create employee
          var emp =
              new EmployeesRow(
                  2001,
                  "B",
                  "QA",
                  "EU",
                  "Bob Smith",
                  Optional.of(new BigDecimal("75000")),
                  LocalDate.of(2024, 6, 1));
          employeesRepo.insert(emp, c);

          // Select by composite ID
          var id = new EmployeesId(2001, "B");
          var found = employeesRepo.selectById(id, c);

          assertTrue(found.isPresent());
          assertEquals(Integer.valueOf(2001), found.get().empNumber());
          assertEquals("B", found.get().empSuffix());
          assertEquals("Bob Smith", found.get().empName());
        });
  }

  @Test
  public void testEmployeesCompositeIdFromRow() {
    DuckDbTestHelper.run(
        c -> {
          // Create department
          departmentsRepo.insert(
              new DepartmentsRow("SUPPORT", "US", "Support", Optional.empty()), c);

          // Create employee
          var emp =
              new EmployeesRow(
                  3001, "C", "SUPPORT", "US", "Carol White", Optional.empty(), LocalDate.now());
          var inserted = employeesRepo.insert(emp, c);

          // Get composite ID from row
          var compositeId = inserted.compositeId();
          assertEquals(Integer.valueOf(3001), compositeId.empNumber());
          assertEquals("C", compositeId.empSuffix());

          // Use it to select
          var found = employeesRepo.selectById(compositeId, c);
          assertTrue(found.isPresent());
        });
  }

  @Test
  public void testEmployeesUpdate() {
    DuckDbTestHelper.run(
        c -> {
          // Create department
          departmentsRepo.insert(
              new DepartmentsRow("MGMT", "US", "Management", Optional.empty()), c);

          // Create and update employee
          var emp =
              new EmployeesRow(
                  4001,
                  "D",
                  "MGMT",
                  "US",
                  "David Brown",
                  Optional.of(new BigDecimal("120000")),
                  LocalDate.now());
          var inserted = employeesRepo.insert(emp, c);

          // Update salary
          var updated = inserted.withSalary(Optional.of(new BigDecimal("130000")));
          employeesRepo.update(updated, c);

          var found = employeesRepo.selectById(inserted.compositeId(), c).orElseThrow();
          assertEquals(Optional.of(new BigDecimal("130000.00")), found.salary());
        });
  }

  @Test
  public void testEmployeesDelete() {
    DuckDbTestHelper.run(
        c -> {
          // Create department
          departmentsRepo.insert(
              new DepartmentsRow("TEMP", "TEMP", "Temp Dept", Optional.empty()), c);

          // Create and delete employee
          var emp =
              new EmployeesRow(
                  5001, "X", "TEMP", "TEMP", "To Be Deleted", Optional.empty(), LocalDate.now());
          var inserted = employeesRepo.insert(emp, c);

          boolean deleted = employeesRepo.deleteById(inserted.compositeId(), c);
          assertTrue(deleted);

          var found = employeesRepo.selectById(inserted.compositeId(), c);
          assertFalse(found.isPresent());
        });
  }

  @Test
  public void testEmployeesMultipleSameNumber() {
    DuckDbTestHelper.run(
        c -> {
          // Create department
          departmentsRepo.insert(
              new DepartmentsRow("SHARED", "US", "Shared Dept", Optional.empty()), c);

          // Insert employees with same number but different suffixes
          var emp1 =
              new EmployeesRow(
                  9999, "A", "SHARED", "US", "Employee A", Optional.empty(), LocalDate.now());
          var emp2 =
              new EmployeesRow(
                  9999, "B", "SHARED", "US", "Employee B", Optional.empty(), LocalDate.now());
          var emp3 =
              new EmployeesRow(
                  9999, "C", "SHARED", "US", "Employee C", Optional.empty(), LocalDate.now());

          employeesRepo.insert(emp1, c);
          employeesRepo.insert(emp2, c);
          employeesRepo.insert(emp3, c);

          // Each should be independently retrievable
          assertTrue(employeesRepo.selectById(new EmployeesId(9999, "A"), c).isPresent());
          assertTrue(employeesRepo.selectById(new EmployeesId(9999, "B"), c).isPresent());
          assertTrue(employeesRepo.selectById(new EmployeesId(9999, "C"), c).isPresent());
        });
  }

  // ==================== Composite FK Relationship ====================

  @Test
  public void testEmployeeDepartmentForeignKey() {
    DuckDbTestHelper.run(
        c -> {
          // Create department
          var dept =
              new DepartmentsRow(
                  "FK_TEST", "FK_REGION", "FK Test Dept", Optional.of(new BigDecimal("500000")));
          departmentsRepo.insert(dept, c);

          // Create employee referencing department
          var emp =
              new EmployeesRow(
                  6001,
                  "F",
                  "FK_TEST",
                  "FK_REGION",
                  "FK Employee",
                  Optional.of(new BigDecimal("60000")),
                  LocalDate.now());
          var insertedEmp = employeesRepo.insert(emp, c);

          // Verify FK columns match
          assertEquals(dept.deptCode(), insertedEmp.deptCode());
          assertEquals(dept.deptRegion(), insertedEmp.deptRegion());

          // Can look up the department using employee's FK values
          var foundDept =
              departmentsRepo.selectById(
                  new DepartmentsId(insertedEmp.deptCode(), insertedEmp.deptRegion()), c);
          assertTrue(foundDept.isPresent());
          assertEquals("FK Test Dept", foundDept.get().deptName());
        });
  }
}
