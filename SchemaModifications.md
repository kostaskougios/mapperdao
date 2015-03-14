An entity by default is stored in one table. But by modifying SelectConfig, DeleteConfig, QueryConfig and UpdateConfig, the entity tree can be stored on different tables.

```
val modifications = SchemaModifications(
	name => "tmp_" + name // store entities in tmp_$name table names
)
val sc = SelectConfig.default.copy(schemaModifications = modifications)
val dc = DeleteConfig.default.copy(schemaModifications = modifications)
val qc = QueryConfig.default.copy(schemaModifications = modifications)
val uc = UpdateConfig.default.copy(schemaModifications = modifications, deleteConfig = dc)

...

val a1 = Attribute("a1", "v1")
val a2 = Attribute("a2", "v2")
val a3 = Attribute("a3", "v3")

val p1 = Product("p1", Set(a1, a2))
val p2 = Product("p2", Set(a1, a3))

// will insert data into tmp_Product, tmp_Attribute and tmp_ProductAttribute tables

val List(i1, i2) = mapperDao.insertBatch(uc, ProductEntity, List(p1, p2))

import Query._
queryDao.query(qc, select from ProductEntity).toSet should be(Set(i1, i2))

```

This way, i.e. data can be horizontally partitioned into multiple tables or be inserted on a tmp table and then merged with the main tables.
