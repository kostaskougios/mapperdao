# Best practices on creating dao's using mapperdao #

The examples contain several dao's which follow best practices and use the mixin trains.
Please download the examples at https://code.google.com/p/mapperdao-examples/

## Daos ##

It is adviced that entity mappings are contained in the same package like the Dao's, preferrably in the same file:

```
class OrderDao(mapperDao: MapperDao, queryDao: QueryDao) {
...
}

object OrderEntity extends Entity[Int,SurrogateIntId, Order]("orders") {
...
}

```

## Dao for IntId or LongId entitites ##

The traits SurrogateIntIdCRUD, SurrogateLongIdCRUD, SurrogateSimpleAll  (non transactional)
and TransactionalSurrogateIntIdCRUD, TransactionalSurrogateLongIdCRUD, TransactionalSurrogateSimpleCRUD (transactional)
can be mixed in your dao to add CRUD methods. Those traits need a `val entity =yourentity` reference to your entity's mapping
and provide create(entity) , retrieve(id), update(entity), merge(entity,id) and delete(entity) methods.

For natural keys, similar traits exists, i.e. NaturalIntIdCRUD, TransactionalNaturalIntIdCRUD

```
class OrderDao(mapperDao: MapperDao, queryDao: QueryDao) extends TransactionalSurrogateIntIdCRUD[Order] {
	val entity = OrderEntity
...
```

The transactional traits will require a transaction manager:

```
val myTxManager = Transaction.transactionManager(jdbc)

...
object ProductDaoTransactional extends TransactionalCRUD[Long,NaturalLongId,Product] with All[Long,NaturalLongId,Product] {
	protected val entity = ProductEntity
	protected val queryDao = ...
	protected val mapperDao = ...
	protected val txManager = myTxManager
}
```

Please look at the [transactions wiki](Transactions.md) on details on how to manage transactions.