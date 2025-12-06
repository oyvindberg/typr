---
title: Defaulted types
---

An interesting case is how to model inserting rows into tables with default values.
Typo gives you the option of doing it very explicitly with the `Defaulted` type.

Here it is:

```scala
sealed trait Defaulted[+T]

object Defaulted {
  case class Provided[T](value: T) extends Defaulted[T]
  case object UseDefault extends Defaulted[Nothing]
  
  /// json instances only. repositories transfer only values you have provided in the insert
}
```

It is used only in "Unsaved" row types, which are perfect for talking about not persisted rows.

For instance:

```scala
import adventureworks.customtypes.Defaulted
import adventureworks.customtypes.TypoBytea
import adventureworks.customtypes.TypoLocalDateTime
import adventureworks.customtypes.TypoUUID
import adventureworks.person.stateprovince.StateprovinceId
import adventureworks.person.address.{AddressId, AddressRow}

/** This class corresponds to a row in table `person.address` which has not been persisted yet */
case class AddressRowUnsaved(
  /** First street address line. */
  addressline1: /* max 60 chars */ String,
  /** Second street address line. */
  addressline2: Option[/* max 60 chars */ String],
  /** Name of the city. */
  city: /* max 30 chars */ String,
  /** Unique identification number for the state or province. Foreign key to StateProvince table.
      Points to [[stateprovince.StateprovinceRow.stateprovinceid]] */
  stateprovinceid: StateprovinceId,
  /** Postal code for the street address. */
  postalcode: /* max 15 chars */ String,
  /** Latitude and longitude of this address. */
  spatiallocation: Option[TypoBytea],
  /** Default: nextval('person.address_addressid_seq'::regclass)
      Primary key for Address records. */
  addressid: Defaulted[AddressId] = Defaulted.UseDefault,
  /** Default: uuid_generate_v1() */
  rowguid: Defaulted[TypoUUID] = Defaulted.UseDefault,
  /** Default: now() */
  modifieddate: Defaulted[TypoLocalDateTime] = Defaulted.UseDefault
) {
  def toRow(addressidDefault: => AddressId, rowguidDefault: => TypoUUID, modifieddateDefault: => TypoLocalDateTime): AddressRow =
    AddressRow(
      addressline1 = addressline1,
      addressline2 = addressline2,
      city = city,
      stateprovinceid = stateprovinceid,
      postalcode = postalcode,
      spatiallocation = spatiallocation,
      addressid = addressid match {
                    case Defaulted.UseDefault => addressidDefault
                    case Defaulted.Provided(value) => value
                  },
      rowguid = rowguid match {
                  case Defaulted.UseDefault => rowguidDefault
                  case Defaulted.Provided(value) => value
                },
      modifieddate = modifieddate match {
                       case Defaulted.UseDefault => modifieddateDefault
                       case Defaulted.Provided(value) => value
                     }
    )
}
object AddressRowUnsaved {
  // also only json instances
}
```

The corresponding repo then exposes these methods:

```scala
import java.sql.Connection

trait AddressRepo {
  def insert(unsaved: AddressRow)(implicit c: Connection): AddressRow
  def insert(unsaved: AddressRowUnsaved)(implicit c: Connection): AddressRow
}
```