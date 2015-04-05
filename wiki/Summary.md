...
  * one-to-many, many-to-one, many-to-many, one-to-one, mapping class hierarchies, support for Options, Calendar, Date and joda DateTime etc
  * type safe, mappings from entities to tables is done in scala typesafe code and query DSL is type safe too
  * a clean domain model
  * no dependencies to mapperdao in domain classes
  * no special rules or restrictions for entities. No reference to mapperdao within entities, no annotations necessary.
  * promotes and encourages immutability for the domain classes. Mutable entities are also supported.
  * cross-database compatibility with no code changes
  * transactions with propagation, isolation levels and time outs
  * supports [integrating legacy code (hibernate,jdbc etc) or other external entities (i.e. entities retrieved via web services)](ExternalEntities.md)
  * the library is not based on reflection
  * DSL for queries which resembles typical select statements. Minimal learning curve.
  * uses spring's JdbcTemplate for low level database access, taking advantage of the api but without the need for mapperdao to be used within spring framework. Connecting to databases, transactions etc can be configured without (and ofcourse within) spring. MapperDao is spring friendly and all spring's database related features can be used when creating dao's with mapperdao.
  * uses joda datetime (immutable date & time classes) to manage datetimes but java.util.Calendar and Date are also supported
  * doesn't require entities to implement equal() or hasCode() methods. MapperDao doesn't use these methods to manage collections.
  * it doesn't require a primary key for each entity
  * supports composite keys and key-less referenced entities for one-to-many and one-to-one
  * it allows lazy-loading or skip-loading of related entities to optimize loading times.
  * supports mapping class hierarchies
  * supports mapping of Java entities and collections