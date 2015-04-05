...
  * 06/10/2013 : 1.0.0.rc25 compiled with scala 2.10.3
  * 18/08/2013 : 1.0.0.rc24 with bug fixes.
  * 11/06/2013 : mapperdao celebrates: it's now 2 years old!
  * 06/06/2013 : 1.0.0.rc23 is now available with [data sharding/horizontal partitioning](SchemaModifications.md) and bug fixes
  * 27/05/2013 : 1.0.0.rc22 is now available with joda updated to v2.2
  * 05/05/2013 : 1.0.0.rc21 is now available with bug fixes, a way of [transforming immutable entity graphs](UpdatingImmutableGraphs.md) and a [workaround for issues with cyclic-dependent entities](CyclicDependencies.md)
  * 15/04/2013 : 1.0.0.rc20 is now available with bug fixes.
  * 31/03/2013 : 1.0.0.rc19 is now available with support for [schemas](Schemas.md), client code can now override mapping of types from scala to database, more joda types support (Period, Duration) and bug fixes
  * 18/03/2013 : 1.0.0.rc18 is now available for scala 2.9.x and 2.10.x. This version is a major refactoring of inserts/updates to use [batches](Batch.md) which greatly improve performance (up to 5x). It should be a drop-in replacement for rc17.
  * 05/01/2013 : 1.0.0.rc17 available for scala 2.10.0
  * 23/12/2012 : 1.0.0.rc17 available for scala 2.10.0-RC1 and RC5
  * 14/12/2012 : 1.0.0.rc17 is now available. Minor bug fixes.
  * 11/11/2012 : 1.0.0.rc16 is now available. This includes 2 new DSL's, one for [Updates](Update.md) and one for [Deletes](Delete.md). Also cpu usage and insert() optimisations.
  * 05/10/2012 : 1.0.0.rc15 is now available. This includes extra type safety but also require a few simple [migration steps](MigrationRC14.md). From this version on, MapperDao compiles in both scala 2.9.2 and the latest 2.10.0 milestone (currently 2.10.0-M7). [Maven configuration instructions](MavenConfiguration.md) is a guide on how to configure mvn/sbt. A new method is now available on mapperDao and crud mixins: merge(). Note: examples, wiki and tutorial were updated to reflect the changes.
  * 18/09/2012 : 1.0.0.rc14 is now available with support for [blobs](Blobs.md) and [sql functions](SqlFunctions.md)
  * 23/08/2012 : 1.0.0.rc13 is now available with declarePrimaryKeys support for one-to-one, bug fixes, queryDao.lowLevelQuery and [an updated tutorial](http://mapperdao-examples.googlecode.com/files/tutorial1.4.pdf)
  * 19/08/2012 : 1.0.0.rc12 is now available with better support for declarePrimaryKeys() when used with many-to-one/one-to-many relationships.
  * 11/08/2012 : 1.0.0.rc11 is now available with support for joda.LocalDate and LocalTime, better support for entities without PK's, performance increase, bug fixes, more scaladocs for public API
  * 02/08/2012 : 1.0.0.rc10 is now available with full support for entities with multiple keys and minor bug fixes
  * 31/07/2012 : 1.0.0 staged at https://oss.sonatype.org/content/repositories/comgooglecodemapperdao-076/ (but not yet available from maven central)
  * 21/07/2012 : migrated to git
  * 03/06/2012 : 1.0.0-rc9 with bug fixes, memory optimizations, configurable lazy loading, better java domain class support, unlink deep unlinks, query.toList method
  * 22/05/2012 : help wanted! If you created a project at home which uses mapperdao and would like to include it to the list of mapperdao examples, please post at the discussion groups!
  * 12/05/2012 : 1.0.0-rc8 with bug fixes and [multi-threaded querying](ConfigurableQueries.md)
  * 06/05/2012 : 1.0.0-rc7 with bug fixes, only-for-query column mappings, entity unlinking to free memory, better support for entities which don't have primary keys, declarePrimaryKeys now uses column mappings.
  * 01/05/2012 : 1.0.0-rc6 with better java support (java primitive types supported and allowing null for primitives), better equality checks for traversables (many-to-many, one-to-many), CRUD mixin enhancement to allow selecting default select/query configs
  * 29/04/2012 : 1.0.0-rc5b which concludes the lazy loading implementation.
  * 28/04/2012 : 1.0.0-rc5a is out with support for more traversable types for lazy loading and mappings. Also a way to lazy load with [pure-scala](LazyLoading.md) code.
  * 26/04/2012 : 1.0.0-rc5 is out with [Lazy loading](LazyLoading.md) for many-to-many, many-to-one, one-to-many.
  * 31/03/2012 : 1.0.0-rc4 is out with bug fixes and [a caching layer](Caching.md).
  * 03/03/2012 : 1.0.0-rc3 is out, bug fixes
  * 13/02/2012 : 1.0.0-rc2 is released which contains further simplification of mappings. Entities don't need the classOf[.md](.md) constructor parameter anymore i.e. instead of `object PersonEntity extends Entity[IntId, Person](classOf[Person])` please use `object PersonEntity extends Entity[IntId, Person]`. Also [tutorial v1.2 pdf](http://mapperdao-examples.googlecode.com/files/tutorial1.2.pdf)
  * 05/02/2012 : [tutorial v1.1 pdf](http://mapperdao-examples.googlecode.com/files/tutorial1.1.pdf)
  * 03/02/2012 : integrating with popular web frameworks is easy with mapperdao : [Integrating with popular web frameworks](IntegrationWithWebFrameworks.md)
  * 03/02/2012 : 1.0.0-rc1 released with [External Entities](ExternalEntities.md) : Ability to map entities that are not handled by mapperdao (i.e. hibernate entities, domain classes that are loaded via jdbc or even classes that are retrieved from a web service), join and perform transactions (where possible) with those.
  * 01/01/2012 : 1.0.0-beta is here! Also the mapperdao discussion group is created at [http://groups.google.com/group/mapperdao](http://groups.google.com/group/mapperdao)
  * 31/12/2011 : a new [mapperdao tutorial (pdf)](http://mapperdao-examples.googlecode.com/files/tutorial.pdf)
  * 04/12/2011 : updated mapperdao's [examples](http://code.google.com/p/mapperdao-examples/) with the productscatalogue example now running on mysql, postgresql, oracle, sqlserver without the need of recompilation. -Ddatabase=X decides which database to use. 1 mapping for all databases.
  * 27/11/2011 : v0.9.2 with support for H2 database
  * 19/11/2011 : v0.9.1 refactored/simplified mappings via a simple DSL
  * 18/11/2011 : v0.9.0 with support for Sql Server
  * 12/11/2011 : v0.8.4 [mapping AnyVal based entities and relationships](SimpleTypesMapping.md) (i.e. `Set[String]`) for manyToMany relationships
  * 06/11/2011 : v0.8.3 [mapping AnyVal based entities and relationships](SimpleTypesMapping.md) (i.e. `Set[String]`) for oneToMany relationships, refactored relationships to use entity objects instead of `classOf[]`
  * 30/10/2011 : v0.8.2 [productcatalogue: circumflex web framework demo](https://code.google.com/p/mapperdao-examples/), [support for Options](OptionSupport.md), `Helper` methods to help web apps updating collections, countAll and countPages for All mixin, QueryDao.count() which counts the rows of a query, queryDao utility methods to fetch 1 result only
  * 22/10/2011 : v0.8.1 adds [configurable updates](ConfigurableCRUD.md)
  * 15/10/2011 : v0.8.0 mostly targeted to ease testing with [mock objects for MapperDao and QueryDao](MockMapperDao.md) and [CRUD mixins](MockDaoMixins.md), [a memory impl for MapperDao](MemoryMapperDao.md) (again useful for testing), entities without PK for one-to-many, incubation of productcatalogue(a web application using circumflex along with mapperdao), before/after callbacks (events) , bug fixes
  * v0.7.0 [derby support](DerbyParticularities.md), [configurable selects](ConfigurableCRUD.md), [queries](ConfigurableQueries.md) and [deletes](ConfigurableCRUD.md), [pagination: retrieve rows with offset and limit](Pagination.md), [mapping class hierarchies](ClassHierarchyMappings.md)
  * v0.6.2 simplified mappings with scala's implicits
  * v0.6.1 support for oracle and [better support for sequences](Sequences.md)
  * v0.6.0 with support for sequences and a [liftweb framework example](https://code.google.com/p/mapperdao-examples/)