Graphs of mutable objects are easy to update as the graph can mutate at any depth. I.e. assuming the following domain model:

```
	case class Product(var name: String, var attributes: Set[Attribute])
	case class Attribute(var name: String, var value: String)
```

then it is easy to load a Product and updating the attributes:

```
	val p=mapperDao.select(ProductsEntity,100) // load product with id of 100
	p.attributes.head.value="new value"
	mapperDao.update(ProductsEntity,p)
```

MapperDao tracks the changes in a similar way that other ORM tool do for mutable entities and it will update the attribute's value
in the database.

But for immutable entities things are not that simple. We can't update the attribute in place. We can only provide a new instance
of the attribute. But we need to let mapperdao know that the new instance is a replacement for the old one. Package class
`com.googlecode.mapperdao` contains the method `replace` which informs mapperdao that a new object replaces an old one.

I.e. for the following domain model:

```
	case class Product(name: String, attributes: Set[Attribute])
	case class Attribute(name: String, value: String)

	import com.googlecode.mapperdao._

	val p=mapperDao.select(ProductsEntity,100) // load product with id of 100
	val newAttributes=p.attributes.map {
		a=>
			replace(a,a.copy(value="new value"))
	}
	val newP=Product(p.name,newAttributes)
	mapperDao.update(ProductsEntity,p,newP)
```

So what we do here is we transform the attributes of p to their new value. On the same time, by using `replace`, we inform
mapperdao that the new attribute values are a replacement for the old ones.

Note: replace must be called on a persisted entity and a non-persisted one.