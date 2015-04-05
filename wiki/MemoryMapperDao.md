## NOTE: This was dropped in rc18 ##

The MemoryMapperDao class provides an alternative way to mock mapperdao for testing. It behaves like mapperdao but it stores data into memory and the data can be retrieved normally as would happen with `mapperdao.select()`.

```
val typeRegistry = TypeRegistry(JobPositionEntity)

val m = MemoryMapperDao(typeRegistry)

val inserted = m.insert(JobPositionEntity, JobPosition("programmer"))
// inserted equals to JobPosition("programmer") with IntId (id=1)
val updated = m.update(JobPositionEntity, inserted, JobPosition("Scala developer"))
// updated equals to JobPosition("Scala developer")
val selected=m.select(JobPositionEntity, inserted.id).get
// selected equals to JobPosition("Scala developer") 
```

Calls to `MemoryMapperDao.insert()` will return entities with any auto-generated int/long values set correctly.

Please note that QueryDao can't be used along with MemoryMapperDao.

[sample](http://code.google.com/p/mapperdao/source/browse/src/test/java/com/googlecode/mapperdao/MemoryMapperDaoSuite.scala)
