MapperDao automated specs can also serve as good usage examples of the library. The code can be browsed at

http://code.google.com/p/mapperdao/source/browse/src/test/scala/com/googlecode/mapperdao/

or checked out from the git repository:

```
git clone https://code.google.com/p/mapperdao/
```

Under src/test directory are several test cases. They consist of the specs class and a sql file.
Typically the class will contain the entity mappings and the end of the file. In the specs class there will be a "createTables"
method which creates the tables (sql scripts naming convention is testclassname.database.sql, i.e. ManyToManySpec.mysql.sql), and also
several test methods. For query specs, in the companion object the queries are declared.

[example](http://code.google.com/p/mapperdao/source/browse/src/test/scala/com/googlecode/mapperdao/SimpleEntitiesSuite.scala)
