1.0.0-rc15 and on includes refactored trait and class names in order to improve type safety and consistency of
mapperdao. Migrating from rc14 to rc15 is easy and requires only a couple of changes in the code.


1.	IntId, LongID, IntIdCRUD, LongIdCrud: add a "Surrogate" in front i.e. SurrogateIntId and SurrogateIntIdCrud. Please look at the new
[Ids](IDS.md) wiki page for more details regarding how Ids can be mapped.

2.	`SimpleEntity[X]` should be replaced with Entity[ID,KEYTYPE,X] where ID,KEYTYPE one of like
Int,SurrogateIntId, Long,SurrogateLongId , Int,NaturalIntId, Long,NaturalLongId , ...
For entities withoud an Id, Unit,NoId should be used. Please check hierarchy of `trait DeclaredIds[T]` for the available key options

3.	Entity[ID,KEY,T] is now used consistently to map entities with natural or surrogate keys. Use NaturalIntId, NaturalStringId etc to
map entities with natural keys, where the key is part of the entity i.e. `Person(id:Int,name:String)`. Use SurrogateIntId and
SurrogateLongId to map entities with surrogate keys i.e. `Person(name:String)`.

Also there are key types for 2 column keys, i.e. `NaturalStringAndStringIds` and `IntAutoAndStringId`.
Please check hierarchy of `trait SimpleId[T]` for the available key options. The mechanism is extensible, please check [Ids](IDS.md).

4.	remove `with Persisted` from constructor methods ,i.e. from

```
def constructor(implicit m) = new Company ... with IntId with Persisted
```

to
```
def constructor(implicit m) = new Company ... with NaturalIntId
```

5.	Dao mixin traits follows the same naming conventions, i.e. IntIdCRUD becomes SurrogateIntIdCRUD.

6.	For CRUD and TransactionalCRUD there are now 3 type parameters, the key type, the key mixin type and the type of the entity , i.e.

```
object ProductDaoTransactional extends TransactionalCRUD[Long, SurrogateLongId, Product] with SurrogateLongIdAll[Product] {
	protected val entity = ProductEntity
	protected val queryDao = DaoMixinsSuite.this.queryDao
	protected val mapperDao = DaoMixinsSuite.this.mapperDao
	protected val txManager = DaoMixinsSuite.this.txManager
}
```

but as discussed previously there are several simplified mixin traits like
SurrogateIntIdCRUD
NaturalIntIdCRUD
NaturalIntIdAll
... and so on, in eclipse you can find the appropriate type if you search classes by db-key-type (Natural, Surrogate), Scala Key tyoe (Int,String,Long),
i.e. NaturalString