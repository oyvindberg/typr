---
title: Open string enums
---

Some people like to use tables and foreign keys to encode enums. It'll typically take this form:

```sql
create table title (code text primary key);
insert into title (code) values ('mr'), ('ms'), ('dr'), ('phd');

create table titledperson
(
    title       text       not null references title,
    name        text       not null
);

```

You can configure typo to generate so-called "open enums" for you. 

```scala
val options = Options(
  // ...
  openEnums = Selector.relationNames("title")
)
```


And typo will output the [Primary key type](./id-types.md) as something like this:

```scala
/** Type for the primary key of table `public.title`. It has some known values: 
  *  - dr
  *  - mr
  *  - ms
  *  - phd
  */
sealed abstract class TitleId(val value: String)

object TitleId {
  def apply(underlying: String): TitleId =
    ByName.getOrElse(underlying, Unknown(underlying))
  case object dr extends TitleId("dr")
  case object mr extends TitleId("mr")
  case object ms extends TitleId("ms")
  case object phd extends TitleId("phd")
  case class Unknown(override val value: String) extends TitleId(value)
  val All: List[TitleId] = List(dr, mr, ms, phd)
  val ByName: Map[String, TitleId] = All.map(x => (x.value, x)).toMap
  
  // type class instances
}
```

## Supported data types

Currently, typo supports the following data types for open enums:
- `text`
- a domain which has `text` as its base type