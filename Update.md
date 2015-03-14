MapperDao has a DSL to update data which resembles update SQL.

```
val pe = ProductEntity

val rowsAffected=(
	update(pe) 
	set pe.name === "fast cpu" 
	where pe.name === "cpu"
).run(queryDao).rowsAffected

println(rowsAffected) // number of rows that were updated
```

To update more than one columns:

```
val he = HusbandEntity

import Update._
val result=(
	update(he)
	set (he.name === "x", he.age === 29)
	where he.age === 30
).run(queryDao)

```

One to one and many-to-one related data can be updated too, since the FK columns are
part of the table to be updated. This is similar to writing update SQL, only columns
belonging to the table can be updated.

many-to-one:

```
case class Person(val name: String, val company: Company)
case class Company(val name: String)
...

val c2 = mapperDao.insert(CompanyEntity, Company("c2"))

import Update._
val pe = PersonEntity
(
	update(pe)
	set pe.company === c2
	where pe.name === "p1"
).run(queryDao)
```

one-to-one:
```
val w3 = mapperDao.insert(WifeEntity, Wife("w3", 25))

val he = HusbandEntity

import Update._
(
	update(he)
	set he.wife === w3
	where he.age === 30
).run(queryDao)

```

Please also see [UpdateSuite](https://code.google.com/p/mapperdao/source/browse/src/test/scala/com/googlecode/mapperdao/UpdateSuite.scala).