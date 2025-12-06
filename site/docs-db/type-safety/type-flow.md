---
title: Type flow
---

Typo follows "dependencies" between PostgreSQL columns (foreign keys and view dependencies) so the
more specific types (like [id types](id-types.md) and [user-selected-types](user-selected-types.md)) can "flow"
downstream from the base column to other tables which reference it.

This makes it way easier to follow these relationships when coding against the generated code.

Information about the dependencies are encoded in comments in the generated code,
look below for things like

```scala
// Points to [[department.DepartmentRow.departmentid]]
```

These are by the way scaladoc links which can be [ctrl-clicked](../other-features/clickable-links.md) in intellij to go to that column.


## Example generated row with type flow

In this example you can see that id types from other tables have "flowed" into this table
through a foreign key, and you can see that the table has a composite id.

```scala
import adventureworks.customtypes.TypoLocalDate
import adventureworks.customtypes.TypoLocalDateTime
import adventureworks.humanresources.employeedepartmenthistory.EmployeedepartmenthistoryId
import adventureworks.humanresources.department.DepartmentId
import adventureworks.humanresources.shift.ShiftId
import adventureworks.person.businessentity.BusinessentityId

case class EmployeedepartmenthistoryRow(
  /** Employee identification number. Foreign key to Employee.BusinessEntityID.
      Points to [[employee.EmployeeRow.businessentityid]] */
  businessentityid: BusinessentityId,
  /** Department in which the employee worked including currently. Foreign key to Department.DepartmentID.
      Points to [[department.DepartmentRow.departmentid]] */
  departmentid: DepartmentId,
  /** Identifies which 8-hour shift the employee works. Foreign key to Shift.Shift.ID.
      Points to [[shift.ShiftRow.shiftid]] */
  shiftid: ShiftId,
  /** Date the employee started work in the department. */
  startdate: TypoLocalDate,
  /** Date the employee left the department. NULL = Current department. */
  enddate: Option[TypoLocalDate],
  modifieddate: TypoLocalDateTime
){
   val compositeId: EmployeedepartmenthistoryId = EmployeedepartmenthistoryId(businessentityid, startdate, departmentid, shiftid)
 }

object EmployeedepartmenthistoryRow {
  // ...instances
}
```
