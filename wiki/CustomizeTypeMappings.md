This can help in cases where the client code needs to be in control of type mappings, i.e. convert all DateTime/Calendars to Long before those been stored or retrieved from the database:

```
val myDatabaseToScalaTypes = new UserDefinedDatabaseToScalaTypes
{
	def scalaToDatabase(data: (SimpleColumn, Any)) = data match {
		/**
		 * we'll convert the time field to a long. We need to inspect column to see
		 * if we're converting for the right entities (otherwise all DateTime fields
		 * for all entities will be converted)
		 */
		case (column: Column, d: DateTime) if (column.entity == DatesEntity) =>
			(column.copy(tpe = classOf[Long]), d.getMillis)

		/**
		 * for every other value, just return it unmodified
		 */
		case v =>
			v
	}

	def databaseToScala(data: (SimpleColumn, Any)) =
		data match {
			/**
			 * The reverse conversion. From database value => scala value.
			 * We need to make sure we do the conversion only for the correct entity.
			 */
			case (column, l: Long) if (column.entity == DatesEntity) =>
				new DateTime(l)
			case (column, value) => value
		}
}
/**
 * now we can initialize mapperDao
 */
val (jdbc, mapperDao, queryDao, _) = Setup.create(Database.byName(database), dataSource, List(DatesEntity), customDatabaseToScalaTypes = myDatabaseToScalaTypes)

```

This way all dates will be stored as long's in the database without having to map all dates to long values.