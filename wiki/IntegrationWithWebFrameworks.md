MapperDao needs only a datasource to be able to work. Here is a list of a couple of web frameworks and how to instantiate mapperdao with those:


### Play! ###

```
// get the default datasource
val dataSource = play.db.DB.getDataSource("default")

// and instantiate mapperdao
val (jdbc, mapperDao, queryDao,transactionManager) = Setup.h2(dataSource, List(ComputerEntity))
```

### Circumflex ###

Circumflex doesn't provide a standarised way to connect to the database, so we're on our own. Here is an example using apache's BasicDataSource:

```
val properties = new Properties
properties.load(getClass.getResourceAsStream("/jdbc.h2.properties"))
val dataSource = BasicDataSourceFactory.createDataSource(properties)
val entities = List(ProductEntity, CategoryEntity, AttributeEntity, PriceEntity)
val (jdbc, mapperDao, queryDao,transactionManager) = Setup(database, dataSource, entities)

```

The properties file :

```
driverClassName=org.h2.Driver
url=jdbc:h2:mem:myDB
username=sa
password=sa
```

### Lift ###

Similar to circumflex.
