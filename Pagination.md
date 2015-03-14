`QueryConfig` class provides configuration for breaking query results into pages or retrieving a range of rows.

In it's raw form, `QueryConfig` can be used as follows:

```
val q=select from p
// run the query, but retrieve only the 2nd and 3rd row
query(QueryConfig(offset = Some(1), limit = Some(2)), q)
```

but the companion object contains helper methods to paginate rows easily:

```
// get 2nd page of data (10 rows), each page contains 10 rows.
// this means rows 10,11,12,...19 will be returned
val entityList=query(QueryConfig.pagination(2, 10),q)

// get 3nd page of data (10 rows), each page contains 10 rows.
// this means rows 20,21,22,...29 will be returned
val entityList=query(QueryConfig.pagination(3, 10),q)

// return 10 rows: 5,6,..14
val entityList=query(QueryConfig.limits(5, 10),q)

```
