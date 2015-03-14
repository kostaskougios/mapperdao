## DataSource ##

You can create a DataSource normally as you would do with Scala or Java. For example, assuming you would like to use
apache dbcp ( http://commons.apache.org/dbcp/ ), the code can look like this:

```
import java.util.Properties
import org.apache.commons.dbcp.BasicDataSourceFactory


val properties = new Properties
properties.load(getClass.getResourceAsStream("/jdbc.test.properties"))
val dataSource = BasicDataSourceFactory.createDataSource(properties)

```

Place the jdbc.test.properties under your classpath. It can look like this:

```
# jdbc connections

driverClassName=org.postgresql.Driver
url=jdbc:postgresql://localhost/MyDatabase
username=testcaseuser
password=secret

```

## Mapper Dao ##

You can configure MapperDao in different ways, the simplest is to use the Setup utility class:

```
import com.googlecode.mapperdao.utils.Setup

// postgresql
val (jdbc,mapperDao,queryDao,txManager) = Setup.postGreSql(dataSource,List(Entity1,Entity2,...))

// mysql
val (jdbc,mapperDao,queryDao,txManager) = Setup.mysql(dataSource,List(Entity1,Entity2,...))

// oracle
val (jdbc,mapperDao,queryDao,txManager) = Setup.oracle(dataSource,List(Entity1,Entity2,...))

// derby
val (jdbc,mapperDao,queryDao,txManager) = Setup.derby(dataSource,List(Entity1,Entity2,...))

// sql server
val (jdbc,mapperDao,queryDao,txManager) = Setup.sqlServer(dataSource,List(Entity1,Entity2,...))

// h2
val (jdbc,mapperDao,queryDao,txManager) = Setup.h2(dataSource,List(Entity1,Entity2,...))

```

**jdbc** allows plain queries & updates to the database. Internally it uses JdbcTemplate from spring framework and hence it takes advantage of transaction and database support from spring.

**mapperDao** can be used for CRUD operations for entities.

**queryDao** can be used to query for entities.

**txManager** can be used to create transactions

### Creating the beans manually ###

You can also go all the way creating the instances. First you need to configure the typeRegistry where you declare all your entities, then a jdbc to connect to the dataSource and then the driver to use. A plain example follows.

```
import com.googlecode.mapperdao.jdbc._
import com.googlecode.mapperdao._
import com.googlecode.mapperdao.drivers.PostgreSql // we'll use the PostgreSql driver

val dataSource = ... as described above

val typeRegistry = TypeRegistry(HouseEntity, PersonEntity)
val typeManager = new DefaultTypeManager
val jdbc=new Jdbc(dataSource, typeManager)
val driver=new PostgreSql(jdbc, typeRegistry)
val mapperDao = new MapperDao(driver)
```

You can create as many mapperDao instances as you need. I.e. you can have different mappings registered to different TypeRegistry's and use those registries to configure mapperDao's. Or if you need to connect to different databases, you can use 2 or more data sources and the same TypeRegistry to create MapperDao's that connect to the different databases.

## QueryDao ##

Configuring a QueryDao is simple:

```
import com.googlecode.mapperdao._

val queryDao=new QueryDao(mapperDao)
```

This queryDao will use the same dataSource as your mapperDao

## Logging ##
MapperDao uses [slf4j](http://www.slf4j.org/) for logging. Please check slf4j documentation on how to configure logging.

MapperDao logs using DEBUG level. The logged statements will look like
```
DEBUG com.googlecode.mapperdao.jdbc.Jdbc - sql:
insert into Inventory(id,stock,product_id)
values(10,5,1)
```

Please note that the values are inlined, but that's only for logging. In the above example, MapperDao issued the command

```
insert into Inventory(id,stock,product_id)
values(?,?,?)
```

with a parameter list `[10,5,1]` but when it logs it, to ease debugging, it shows the values in-lined along with the sql.

MapperDao logger is set to use DEBUG level and can be disabled if slf4j is configured appropriately.