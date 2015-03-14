### Building queries ###

Queries can be constructed dynamically:

```
import com.googlecode.mapperdao.Query._

val qm = (
	select
	from pe
	where pe.name === "x"
)

val q1 = extend(qm) join(pe, pe.company, ce)
val q = extend(q1) and ce.name === "y"

```

Queries are immutable, so qm, q1 and q are different queries (a change since 1.0.0.rc25 where queries were a mutable builder)

### Documentation for version up to 1.0.0.rc25 ###

Queries can be build dynamically. When you obtain a reference to a query, you can later on use it to add additional expressions like:

```
val q = select from jpe where jpe.id === 1
q or jpe.id === 2
q or (jpe.id === 5 or jpe.id === 6)
q orderBy (jpe.id, desc)
```

So effectively you can build your query based on conditions or even multiple methods can contribute to it:

```
val q = select from jpe where jpe.id === 1

if(today==MONDAY) q or jpe.id === 2 else q or (jpe.id === 5 or jpe.id === 6)

q orderBy (jpe.id, desc)

```

To dynamically modify where and joins:

```
val q=(select from jpe)
val w= (q where)
w(jpe.name==="name1")
w.or(jpe.name==="name2")
q.join(....)
```
val q=(select from jpe)
val w= (q where)
w(jpe.name==="name1")
w.or(jpe.name==="name2")
q.join(....)
}}}```