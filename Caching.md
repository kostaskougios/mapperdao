MapperDao supports caching on the driver level. The caching layer caches data as they are returned from queries.
Assuming you have a Product entity and a Product table, mapperdao caches the raw results of the queries to Product table.
MapperDao doesn't cache the entities themselfs.

MapperDao caching has it's own expiration mechanism. Every select/query can provide a dt in milliseconds. If the cached data
are older than dt they are refreshed from the database. If not then they are used.

Different queries can declare a different dt value. For example, for a product listing page we would like to avoid querying
the database for every page view. We can set the dt=10000 and effectively the data will be read from the database once per
10 seconds. But in the product details page, we would like to show up to date information about the products and it would be
better if dt=2000 so that the data will be refreshed every 2 seconds.

The caching layer, on the current mapperdao implementation, doesn't update or flush the cached values on update or delete. This might
change in the future.

## Configuration ##

### EHCache ###

```
import net.sf.ehcache.CacheManager
import com.googlecode.mapperdao.ehcache._

val cacheManager = CacheManager.create
val ehCache = cacheManager.getCache("MyCache")
val mapperDaoCache = new CacheUsingEHCache(ehCache) with Locking

... create datasource etc ...

val (jdbc,mapperDao,queryDao,txManager) = Setup.mysql(dataSource,List(Entity1,Entity2,...),Some(mapperDaoCache))

```

CacheUsingEHCache provides caching and the Locking trait ensures that when the same key is requested more than once on the same time, only once it will be retrieved from the database.

A simple ehcache configration file is provided (ehcache.xml should be in the root of the classpath) :

```
<ehcache>
    <cache  name="MyCache"
            maxElementsInMemory="10000"
            eternal="false"
            timeToIdleSeconds="5"
            timeToLiveSeconds="5"
            overflowToDisk="false"
            diskPersistent="false"
            memoryStoreEvictionPolicy="LRU" />
</ehcache>
```

## Usage ##

### Selecting ###

Each select() can be configured to use caching or not. A SelectConfig should be provided with the desired caching configuration:

```
// (A) use cached data that are up to 1 hour old
mapperDao.select(SelectConfig(cacheOptions = CacheOptions.OneHour), ProductEntity, 5)

// (B) use cached data that are up to 1000 ms old
mapperDao.select(SelectConfig(cacheOptions = CacheOptions(1000)), ProductEntity, 5)

// (C) do not use cached data, read the entity straight from the database
mapperDao.select(SelectConfig(cacheOptions = NoCache), ProductEntity, 5)
// (D) or...
mapperDao.select(ProductEntity, 5)

```

In the above examples, each select() is configured differently. So, even if the cache contains the data, the last select() will read
the entity straight from the database.

(A) and (B) will use the cached data only if they are newer than specified. Even if the cache contains the data but the data are older
than 1 hour, then they are not going to be used. If the data are up to 1 minute old, (A) will use them but (B) will retrieve them from the database.

### Querying ###

Similarily to select(), the queries can also use cached data to avoid the trip to the database.

```
// use cached data up to 1 hour old
val list=queryDao.query(QueryConfig(cacheOptions = CacheOptions.OneHour), select from ProductEntity)
```
