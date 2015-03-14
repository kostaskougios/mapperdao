# Transactions #

## Transaction Manager ##

The 1st step is to create 1 Transaction Manager per database. This is done during [mapperdao initialisation](SetupDaos.md) :

```

val (jdbc,mapperDao,queryDao,txManager) = Setup.postGreSql(dataSource,List(Entity1,Entity2,...))

```

Also, assuming you  [already have a jdbc](SetupDaos.md), you can then create the transaction manager:

```
import com.googlecode.mapperdao.jdbc.Transaction

val txManager = Transaction.transactionManager(jdbc)
```

`txManager` can be reused and it is thread safe.

## Transactions ##

You can now use this txManager to run operations within a transaction:

```
import com.googlecode.mapperdao.jdbc.Transaction
import Transaction._

val tx = Transaction.get(txManager, Propagation.Nested, Isolation.ReadCommited, -1 /* timeout in secs, -1 never times out */)

val inserted = tx { () =>
	// TransactionBlock
	val date = DateTime.now
	val inserted = mapperDao.insert(JobPositionEntity, new JobPosition(5, "Developer", date, date - 2.months, 10))
...
	inserted
}

```

If TransactionBlock throws an exception, the transaction will roll back. Please note that tx can be reused and it is thread safe (though TransactionBlock should not create new threads)

  * **Transaction.Propagation** defines different transaction propagation levels
  * **Transaction.Isolation** defines different transaction isolation levels

You can also easily get the highest level for a transaction:
```
val tx=Transaction.highest(txManager)
```
or the default levels for your database:
```
val tx=Transaction.default(txManager)
```

Please note that mapperdao uses spring's JdbcTemplate and it's excellent transaction support. That can be used with or without spring framework. All declarative transaction configuration of spring works with mapperdao.

## Manually rolling back ##

You can manually roll back a transaction:

```
tx { status =>
...
	status.setRollbackOnly
...
}
```
