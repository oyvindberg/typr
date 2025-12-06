---
title: "Patterns: Dynamic queries"
---

If you use [sql files](../what-is/sql-is-king.md), there is a very high chance you'll want some queries to
a bit dynamic. The way forward here is to move the dynamism into the sql itself. 

A frequently used pattern is a query with an optional filter that selects all rows by default. This can be achieved using a IS NULL construct. 
Here is an example of a query with optional age and name filters:

```sql
SELECT p.title, p.firstname, p.middlename, p.lastname
FROM person.person p
WHERE :"first_name?" = p.firstname OR :first_name IS NULL 
```

Will generate this repo:
```scala
import adventureworks.person_dynamic.PersonDynamicSqlRow
import java.sql.Connection

trait PersonDynamicSqlRepo {
  def apply(firstName: Option[String])(implicit c: Connection): List[PersonDynamicSqlRow]
}
```

Note that the sql query needs an explicit cast to figure out the type of the `first_name` parameter since it's compared with `NULL`.

## What can be dynamic?
You can only use this mechanism for this which are templated into SQL as parameters. 
It's not possible to use it decide keywords, column names and so on, unfortunately.
