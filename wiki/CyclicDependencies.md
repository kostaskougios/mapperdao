When entity A depends on B and B depends on A then scala might not be able to initialize the entity objects. In that
case, there is a workaround by using `EntityRef`. EntityRef allows a reference to an entity to be provided, breaking the
cycle of references. Here is an example:

```
	case class Tag(tag: String, product: Product)

	case class Product(refCode: String, name: String,tags:Set[Tag])

	object TagEntity extends Entity[(String, Product with NaturalStringId), With2Ids[String, Product with NaturalStringId], Tag]
	{
		val tag = key("tag") to (_.tag)
		val product = manytoone(SimpleProductEntity) to (_.product)

		declarePrimaryKey(product)

		def constructor(implicit m: ValuesMap) = new Tag(tag, product) with Stored
	}

	object ProductEntity extends extends Entity[String, NaturalStringId, Product]
	{
		val TagEntityRef = EntityRef("Tag", classOf[Tag], TagEntity)

		val name = column("name") to (_.name)
		val refCode = key("refCode") to (_.refCode)
		val tags = onetomany(TagEntityRef) to (ce => Nil)

		def constructor(implicit m: ValuesMap) = new Product(refCode, name,tags) with Stored
	}
```