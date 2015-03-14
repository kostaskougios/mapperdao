MapperDao supports type-safe mapping of functions to scala objects.

### Example 1 ###

Lets have a look at the following postgresql function:

```
CREATE or replace FUNCTION isAcme(IN cname varchar(50)) RETURNS boolean AS
$$
begin
	return cname = 'acme ltd';
end
$$

LANGUAGE plpgsql VOLATILE;
```

We need to map the input parameter as String and the return type as Boolean:

```
val isAcme = SqlFunction.with1Arg[String, Boolean]("isAcme")
```

Now we can use it in a query:

```
import Query._
(
	select
	from ce
	where isAcme("company A")
).toSet(queryDao)
```

The above call will be translated to this query:

```
	select ....
	from Company
	where isAcme("company A")
```

Please note that boolean return types are not available in all databases. Some databases represent boolean types with 0 or 1 and
the query should be modified to match against that: ` isAcme("company A") === 1 `

### Example 2 ###

Lets have a look at the following postgresql function that has 2 int inputs and returns an int:

```
CREATE or replace FUNCTION addition(IN v int, IN howMany int) RETURNS int AS
$$
begin
	return v + howMany;
end
$$

LANGUAGE plpgsql VOLATILE;
```

We can map it via the following:

```
val addFunction = SqlFunction.with2Args[Int, Int, Int]("addition")
```

We declared that the function takes 2 int parameters and return 1 int result: `SqlFunction.with2Args[Int, Int, Int]`.
We can now use it in queries:

```
import Query._
(select
	from he
	where addFunction(1, 1) === 2
).toSet(queryDao)
```

The above call will be translated to this query:

```
	select ....
	from Human
	where addFunction(1, 1) = 2
```

### Nesting Functions ###

Functions can be nested when used in queries, just as it would be done when writing sql :

```
val addFunction = SqlFunction.with2Args[Int, Int, Int]("addition")
val subFunction = SqlFunction.with2Args[Int, Int, Int]("sub")

import Query._
(
	select
	from ce
	where addFunction(1, subFunction(10, 9)) === 2
).toSet(queryDao)
```

The above call will be translated to this query:

```
	select ....
	from Company
	where addFunction(1, subFunction(10, 9)) = 2
```

### Passing column values to functions ###

All column() mappings can be used as parameters to functions, provided that they are of the correct type:

```
import Query._
(select
	from he
	where addFunction(he.age, 10) > 22
).toSet(queryDao)
```

The above query will return all entities where `he.age + 10 > 22`. It will be translated to this query:

```
	select ....
	from Human he
	where addFunction(he.age, 10) > 22
```

### Passing related columns to functions ###

For many-to-one and one-to-one relationships, the foreign key is part of the mapped table. Hence it can be used when calling
functions and the related entity's id will be passed as a parameter to the call:

```
import Query._
(
	select
	from pe
	where addFunction(pe.company, 2) > 3
).toSet(queryDao)
```

The above call will be translated to this query:

```
	select ....
	from Person
	where addFunction(pe.company_id, 2) > 3
```


### Full example ###
[SqlFunctionSuite](https://code.google.com/p/mapperdao/source/browse/src/test/scala/com/googlecode/mapperdao/SqlFunctionSuite.scala)

### Library of Std Sql functions ###

Please `import StdSqlFunctions._` and use one of the available functions.