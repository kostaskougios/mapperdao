MapperDao features:

  * drivers for oracle, postgresql, mysql, derby, sql server, h2
  * one-to-one, one-to-many, many-to-one and many-to-many
  * transactions (spring's JdbcTemplate is used for low level database operations)
  * auto-generated columns and sequences
  * encourages immutability (but ofcourse mutable domain classes are supported)
  * clean domain models
  * cross-database compatibility, map once and use with all
  * multiple primary keys
  * query DSL that resembles select statements
  * traits to help dao creation (mixin with dao's)
  * doesn't depend on object equality or hashcode
  * configurable selects, queries & delete propagation
  * data shredding

MapperDao makes CRUD operations, queries and transactions very simple and it follows a no-surprise approach. As with any ORM tool,
the mappings are probably the complex part of the library but they are done in a type-safe manner and most errors will be caught
during compilation. Mapping is scala code and doesn't use xml or annotations which clutter the domain classes.

[more...](Summary.md)