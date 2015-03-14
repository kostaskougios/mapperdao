## skip loading certain relationships ##

A `QueryConfig` can be used to fine tune data retrieval of queries:

```
val p=ProductEntity
val a=AttributeEntity
val q=select from p join (p, p.attributes, a) where a.value === "46'"
val results=query(QueryConfig(skip = Set(ProductEntity.attributes)), q)
```

This will query for all products containing an attribute with value of `46'`, but without retrieving the `product.attribute` relationship.

The important bit here is

```
QueryConfig(skip = Set(ProductEntity.attributes))
```

This makes mapperdao to skip loading ProductEntity.attributes when running the query (but `where a.value === "46'"` is allowed)

Please note that the `skip` set can contain any relationship that might be used to load the entity from the database. I.e. in the above example, assuming that `Attribute` has `attribute.inventory`, then the following query for Product's will skip from loading `attribute.inventory` relationship.

```
val results=query(QueryConfig(skip = Set(AttributeEntity.inventory)), q)
```

## Using multiple threads when querying ##

Queries load entities and each entity might have to load several other related entities. In certain occasions, i.e. when batch processing the data, you might want to speed up things by running the loading of all these data in parallel. A query can be easily configured to do that:

```
queryDao.query(
	QueryConfig(multi = MultiThreadedConfig.Multi),
	select from ProductEntity orderBy (ProductEntity.name, desc)
)
```

Please look at [MultiThreadedConfig](http://code.google.com/p/mapperdao/source/browse/src/main/scala/com/googlecode/mapperdao/MultiThreadedConfig.scala) class for further options. In the background, mapperdao does the initial query and then uses Scala's parallel collections to fetch all related data. This in some occasions can speed up the query 2x to 5x for large result sets. But please be aware of the following limitations:

  * The load on your database will also be higher for the (shorter) duration of the query.
  * speed doesn't always improve. I.e. for an h2 in-memory database, speed doesn't improve at all. But for most networked connections, speed will improve significantly.
  * if you need to run the query within a transaction, please don't parallelize it. Multi-threaded queries are not done within a transaction and can fetch data that were modified by other transactions