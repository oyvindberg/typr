package testdb;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;
import testdb.customtypes.Defaulted;
import testdb.customtypes.Defaulted.*;
import testdb.departments.*;
import testdb.employees.*;

/**
 * Tests for composite primary keys using the employees table. The employees table has a composite
 * key of (emp_number, emp_suffix) which tests: - Type-safe composite ID wrapper classes - Select by
 * composite ID - Update/Delete with composite keys - selectByIds and selectByIdsTracked with
 * composite IDs
 */
public class CompositeIdTest {
  private final EmployeesRepoImpl empRepo = new EmployeesRepoImpl();
  private final DepartmentsRepoImpl deptRepo = new DepartmentsRepoImpl();

  @Test
  public void testCompositeIdCreation() {
    WithConnection.run(
        c -> {
          var id = new EmployeesId(1001, "A");
          assertEquals(Integer.valueOf(1001), id.empNumber());
          assertEquals("A", id.empSuffix());

          var id2 = new EmployeesId(1001, "B");
          assertNotEquals(id, id2); // Different suffix

          var id3 = new EmployeesId(1002, "A");
          assertNotEquals(id, id3); // Different number

          var id4 = new EmployeesId(1001, "A");
          assertEquals(id, id4); // Same values
        });
  }

  @Test
  public void testInsertAndSelectByCompositeId() {
    WithConnection.run(
        c -> {
          var dept =
              deptRepo.insert(
                  new DepartmentsRow("ENG_TEST", "US-WEST", "Engineering", Optional.empty()), c);

          var unsaved =
              new EmployeesRowUnsaved(
                  100001,
                  "TEST_A",
                  dept.deptCode(),
                  dept.deptRegion(),
                  "John Doe",
                  Optional.of(new BigDecimal("75000.00")),
                  new Defaulted.UseDefault<>());

          var inserted = empRepo.insert(unsaved, c);
          assertEquals(Integer.valueOf(100001), inserted.compositeId().empNumber());
          assertEquals("TEST_A", inserted.compositeId().empSuffix());

          var selected = empRepo.selectById(inserted.compositeId(), c);
          assertTrue(selected.isPresent());
          assertEquals("John Doe", selected.get().empName());
          assertEquals(dept.deptCode(), selected.get().deptCode());
          assertEquals(dept.deptRegion(), selected.get().deptRegion());
          assertEquals(new BigDecimal("75000.00"), selected.get().salary().get());
        });
  }

  @Test
  public void testSelectByDifferentCompositeIds() {
    WithConnection.run(
        c -> {
          var dept =
              deptRepo.insert(new DepartmentsRow("SALES", "US-EAST", "Sales", Optional.empty()), c);

          // Insert employees with same number but different suffix
          var emp1 =
              empRepo.insert(
                  new EmployeesRowUnsaved(
                      2001,
                      "X",
                      dept.deptCode(),
                      dept.deptRegion(),
                      "Alice Smith",
                      Optional.of(new BigDecimal("60000")),
                      new Defaulted.UseDefault<>()),
                  c);

          var emp2 =
              empRepo.insert(
                  new EmployeesRowUnsaved(
                      2001,
                      "Y",
                      dept.deptCode(),
                      dept.deptRegion(),
                      "Bob Jones",
                      Optional.of(new BigDecimal("65000")),
                      new Defaulted.UseDefault<>()),
                  c);

          // Should be able to select both independently
          var selectedX = empRepo.selectById(new EmployeesId(2001, "X"), c);
          var selectedY = empRepo.selectById(new EmployeesId(2001, "Y"), c);

          assertTrue(selectedX.isPresent());
          assertTrue(selectedY.isPresent());
          assertEquals("Alice Smith", selectedX.get().empName());
          assertEquals("Bob Jones", selectedY.get().empName());
          assertNotEquals(selectedX.get().compositeId(), selectedY.get().compositeId());
        });
  }

  @Test
  public void testSelectByCompositeIds() {
    WithConnection.run(
        c -> {
          var dept =
              deptRepo.insert(new DepartmentsRow("IT_CODE", "US-WEST", "IT", Optional.empty()), c);

          var emp1 =
              empRepo.insert(
                  new EmployeesRowUnsaved(
                      3001,
                      "A",
                      dept.deptCode(),
                      dept.deptRegion(),
                      "Charlie Brown",
                      Optional.of(new BigDecimal("70000")),
                      new Defaulted.UseDefault<>()),
                  c);
          var emp2 =
              empRepo.insert(
                  new EmployeesRowUnsaved(
                      3002,
                      "B",
                      dept.deptCode(),
                      dept.deptRegion(),
                      "Diana Prince",
                      Optional.of(new BigDecimal("80000")),
                      new Defaulted.UseDefault<>()),
                  c);
          var emp3 =
              empRepo.insert(
                  new EmployeesRowUnsaved(
                      3003,
                      "C",
                      dept.deptCode(),
                      dept.deptRegion(),
                      "Eve Adams",
                      Optional.of(new BigDecimal("75000")),
                      new Defaulted.UseDefault<>()),
                  c);

          var ids = new EmployeesId[] {new EmployeesId(3001, "A"), new EmployeesId(3003, "C")};

          var selected = empRepo.selectByIds(ids, c);
          assertEquals((long) 2, selected.size());
          assertTrue(selected.stream().anyMatch(e -> e.empName().equals("Charlie Brown")));
          assertTrue(selected.stream().anyMatch(e -> e.empName().equals("Eve Adams")));
          assertFalse(selected.stream().anyMatch(e -> e.empName().equals("Diana Prince")));
        });
  }

  @Test
  public void testSelectByCompositeIdsTracked() {
    WithConnection.run(
        c -> {
          var dept =
              deptRepo.insert(new DepartmentsRow("HR_CODE", "US-WEST", "HR", Optional.empty()), c);

          var emp1 =
              empRepo.insert(
                  new EmployeesRowUnsaved(
                      4001,
                      "X",
                      dept.deptCode(),
                      dept.deptRegion(),
                      "Frank Miller",
                      Optional.of(new BigDecimal("72000.00")),
                      new Defaulted.UseDefault<>()),
                  c);
          var emp2 =
              empRepo.insert(
                  new EmployeesRowUnsaved(
                      4002,
                      "Y",
                      dept.deptCode(),
                      dept.deptRegion(),
                      "Grace Hopper",
                      Optional.of(new BigDecimal("95000")),
                      new Defaulted.UseDefault<>()),
                  c);

          var ids = new EmployeesId[] {new EmployeesId(4001, "X"), new EmployeesId(4002, "Y")};

          Map<EmployeesId, EmployeesRow> tracked = empRepo.selectByIdsTracked(ids, c);
          assertEquals((long) 2, tracked.size());

          assertEquals("Frank Miller", tracked.get(new EmployeesId(4001, "X")).empName());
          assertEquals("Grace Hopper", tracked.get(new EmployeesId(4002, "Y")).empName());
          assertEquals(
              new BigDecimal("72000.00"), tracked.get(new EmployeesId(4001, "X")).salary().get());
          assertEquals(
              new BigDecimal("95000.00"), tracked.get(new EmployeesId(4002, "Y")).salary().get());
        });
  }

  @Test
  public void testUpdateWithCompositeId() {
    WithConnection.run(
        c -> {
          var dept =
              deptRepo.insert(
                  new DepartmentsRow("Finance_CODE", "US-WEST", "Finance", Optional.empty()), c);

          var inserted =
              empRepo.insert(
                  new EmployeesRowUnsaved(
                      5001,
                      "A",
                      dept.deptCode(),
                      dept.deptRegion(),
                      "Before Update",
                      Optional.of(new BigDecimal("50000")),
                      new UseDefault<>()),
                  c);

          var updated =
              inserted
                  .withEmpName("After Changed")
                  .withSalary(Optional.of(new BigDecimal("90000.00")));

          assertTrue(empRepo.update(updated, c));

          var selected = empRepo.selectById(inserted.compositeId(), c);
          assertTrue(selected.isPresent());
          assertEquals("After Changed", selected.get().empName());
          assertEquals(new BigDecimal("90000.00"), selected.get().salary().get());
        });
  }

  @Test
  public void testDeleteByCompositeId() {
    WithConnection.run(
        c -> {
          var dept =
              deptRepo.insert(
                  new DepartmentsRow("Marketing_CODE", "US-WEST", "Marketing", Optional.empty()),
                  c);

          var inserted =
              empRepo.insert(
                  new EmployeesRowUnsaved(
                      6001,
                      "Z",
                      dept.deptCode(),
                      dept.deptRegion(),
                      "Delete Me",
                      Optional.of(new BigDecimal("55000")),
                      new UseDefault<>()),
                  c);

          assertTrue(empRepo.deleteById(inserted.compositeId(), c));
          assertFalse(empRepo.selectById(inserted.compositeId(), c).isPresent());
        });
  }

  @Test
  public void testDeleteByCompositeIds() {
    WithConnection.run(
        c -> {
          var dept =
              deptRepo.insert(
                  new DepartmentsRow("Operations_CODE", "US-WEST", "Operations", Optional.empty()),
                  c);

          var emp1 =
              empRepo.insert(
                  new EmployeesRowUnsaved(
                      7001,
                      "A",
                      dept.deptCode(),
                      dept.deptRegion(),
                      "Delete1 Test",
                      Optional.of(new BigDecimal("60000")),
                      new UseDefault<>()),
                  c);
          var emp2 =
              empRepo.insert(
                  new EmployeesRowUnsaved(
                      7002,
                      "B",
                      dept.deptCode(),
                      dept.deptRegion(),
                      "Delete2 Test",
                      Optional.of(new BigDecimal("62000")),
                      new UseDefault<>()),
                  c);
          var emp3 =
              empRepo.insert(
                  new EmployeesRowUnsaved(
                      7003,
                      "C",
                      dept.deptCode(),
                      dept.deptRegion(),
                      "Keep Me",
                      Optional.of(new BigDecimal("64000")),
                      new UseDefault<>()),
                  c);

          var idsToDelete =
              new EmployeesId[] {new EmployeesId(7001, "A"), new EmployeesId(7002, "B")};

          var deleteCount = empRepo.deleteByIds(idsToDelete, c);
          assertEquals(Integer.valueOf(2), deleteCount);

          assertFalse(empRepo.selectById(new EmployeesId(7001, "A"), c).isPresent());
          assertFalse(empRepo.selectById(new EmployeesId(7002, "B"), c).isPresent());
          assertTrue(empRepo.selectById(new EmployeesId(7003, "C"), c).isPresent());
        });
  }

  @Test
  public void testDSLSelectWithCompositeId() {
    WithConnection.run(
        c -> {
          var dept =
              deptRepo.insert(
                  new DepartmentsRow("Research_CODE", "US-WEST", "Research", Optional.empty()), c);

          empRepo.insert(
              new EmployeesRowUnsaved(
                  8001,
                  "A",
                  dept.deptCode(),
                  dept.deptRegion(),
                  "Alice DSL",
                  Optional.of(new BigDecimal("85000")),
                  new UseDefault<>()),
              c);
          empRepo.insert(
              new EmployeesRowUnsaved(
                  8001,
                  "B",
                  dept.deptCode(),
                  dept.deptRegion(),
                  "Bob DSL",
                  Optional.of(new BigDecimal("87000")),
                  new UseDefault<>()),
              c);

          // Select by emp_number (part of composite key)
          var empNum8001 = empRepo.select().where(f -> f.empNumber().isEqual(8001)).toList(c);
          assertEquals((long) 2, empNum8001.size());

          // Select by emp_suffix (part of composite key)
          var suffixA = empRepo.select().where(f -> f.empSuffix().isEqual("A")).toList(c);
          assertTrue(suffixA.size() >= 1);
          assertTrue(suffixA.stream().anyMatch(e -> e.empName().equals("Alice DSL")));

          // Select by both parts of composite key
          var specific =
              empRepo
                  .select()
                  .where(f -> f.empNumber().isEqual(8001))
                  .where(f -> f.empSuffix().isEqual("B"))
                  .toList(c);
          assertEquals((long) 1, specific.size());
          assertEquals("Bob DSL", specific.get(0).empName());
        });
  }

  @Test
  public void testDSLUpdateWithCompositeId() {
    WithConnection.run(
        c -> {
          var dept =
              deptRepo.insert(
                  new DepartmentsRow("Legal_CODE", "US-WEST", "Legal", Optional.empty()), c);

          var inserted =
              empRepo.insert(
                  new EmployeesRowUnsaved(
                      9001,
                      "X",
                      dept.deptCode(),
                      dept.deptRegion(),
                      "Before DSL",
                      Optional.of(new BigDecimal("70000")),
                      new UseDefault<>()),
                  c);

          empRepo
              .update()
              .setValue(f -> f.empName(), "After")
              .setValue(f -> f.salary(), new BigDecimal("100000.00"))
              .where(f -> f.empNumber().isEqual(9001))
              .where(f -> f.empSuffix().isEqual("X"))
              .execute(c);

          var updated = empRepo.selectById(inserted.compositeId(), c);
          assertTrue(updated.isPresent());
          assertEquals("After", updated.get().empName());
          assertEquals(new BigDecimal("100000.00"), updated.get().salary().get());
        });
  }

  @Test
  public void testForeignKeyToDepartment() {
    WithConnection.run(
        c -> {
          var engineeringDept =
              deptRepo.insert(
                  new DepartmentsRow(
                      "Engineering_CODE", "US-WEST", "Engineering", Optional.empty()),
                  c);
          var salesDept =
              deptRepo.insert(
                  new DepartmentsRow("Sales_CODE", "US-WEST", "Sales", Optional.empty()), c);

          var engineer =
              empRepo.insert(
                  new EmployeesRowUnsaved(
                      10001,
                      "E",
                      engineeringDept.deptCode(),
                      engineeringDept.deptRegion(),
                      "Engineer Person",
                      Optional.of(new BigDecimal("90000")),
                      new UseDefault<>()),
                  c);

          var salesperson =
              empRepo.insert(
                  new EmployeesRowUnsaved(
                      10002,
                      "S",
                      salesDept.deptCode(),
                      salesDept.deptRegion(),
                      "Sales Person",
                      Optional.of(new BigDecimal("70000")),
                      new UseDefault<>()),
                  c);

          // Verify foreign key relationships
          assertEquals(engineeringDept.deptCode(), engineer.deptCode());
          assertEquals(salesDept.deptCode(), salesperson.deptCode());

          // Select employees by department using DSL
          var engineeringEmployees =
              empRepo
                  .select()
                  .where(f -> f.deptCode().isEqual(engineeringDept.deptCode()))
                  .toList(c);
          assertTrue(engineeringEmployees.size() >= 1);
          assertTrue(
              engineeringEmployees.stream().anyMatch(e -> e.empName().equals("Engineer Person")));
        });
  }
}
