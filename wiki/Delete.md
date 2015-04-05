MapperDao has a DSL to delete data which resembles delete SQL.

```
import Delete._
val pe = ProductEntity
val rowsAffected=(
	delete from pe 
	where pe.name === "cpu"
).run(queryDao).rowsAffected

println(rowsAffected) // number of deleted rows
```

Many to one and one to one related data can be used in the criteria as it would
in a delete SQL:

```
import Delete._
val pe = PersonEntity
(
	delete from pe 
	where pe.company === c1
).run(queryDao)
```

Please note: MapperDao doesn't delete related entity data. Foreign key "on delete cascade" should
be used.

Please also see [DeleteSuite](https://code.google.com/p/mapperdao/source/browse/src/test/scala/com/googlecode/mapperdao/DeleteSuite.scala).