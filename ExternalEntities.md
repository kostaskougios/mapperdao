### Mapping of External Entities ###

MapperDao can integrate with classes that are managed by hibernate, plain jdbc or anything else from web services to entities stored in flat files. Relationships between those external entities and mapperdao entities can be created and all relationship types are supported. Querying with joins on those entities and transactions are also supported provided some criteria are meet.

For example, lets assume that we want to map these two classes in a one to one relationship :

```
case class Product(val id: Int, val inventory: Inventory)
case class Inventory(val id: Int, val stock: Int)
```

Product class will be managed by mapperdao but Inventory class won't. In this case, Inventory class will be "stored" in a hashmap.

In such case, only the Product table will exist in the database :

```
create table Product (
	id int not null,
	primary key (id)
)
```

The mappings follows. The mapping of Product to ProductEntity is standard mapping, nothing weird here:

```
object ProductEntity extends Entity[Int,NaturalIntId,Product] {
	val id = key("id") to (_.id)
	val inventory = onetoonereverse(InventoryEntity) to (_.inventory)

	def constructor(implicit m) = new Product(id, inventory) with Stored
}

```

The mapping of Inventory is done again via an InventoryEntity. This time though, the mapping extends ExternalEntity.
Also for this entity, we will provide a simple map based in memory implementation:

```
object InventoryEntity extends ExternalEntity[Int,Inventory] {

	// we need a map to store the in-memory entity. The key is
	// the *product* id and the value is the related Inventory
	var inventory = Map[Int, Inventory]()

	// now we need to add a few handlers when various events occur:

	// when an insert of ProductEntity occurs, and for the
	// ProductEntity.inventory column, invoke this handler:
	onInsertOneToOneReverse(ProductEntity.inventory) { i =>
		// just add the entity into the map
		inventory = inventory + (i.entity.id -> i.foreign)
	}

	// when a product entity is selected, we need to fetch
	// the related Inventory entity from the map by the
	// foreignId (in this case the product id)
	onSelectOneToOneReverse(ProductEntity.inventory) {
		// foreignIds is a list with the Product primary keys,
		// in this case just 1 Int with product.id
		_.foreignIds match {
			case (foreignId: Int) :: Nil => inventory(foreignId)
		}
	}

	// when an update of the product entity occurs, we need to
	// update the relevant Inventory entity
	onUpdateOneToOneReverse(ProductEntity.inventory) { u =>
		// u.entity == the product been updated
		inventory = inventory + (u.entity.id -> u.foreign)
	}

	// when a product is deleted, we'll remove the relevant
	// inventory
	onDeleteOneToOneReverse(ProductEntity.inventory) { d =>
		inventory -= d.entity.id
	}
}
```

### More examples ###

Please have a look at the following test suites. At the end of the file, the entities and external entities are declared.

[many to many](https://code.google.com/p/mapperdao/source/browse/src/test/scala/com/googlecode/mapperdao/ManyToManyExternalEntitySuite.scala)

[many to one](https://code.google.com/p/mapperdao/source/browse/src/test/scala/com/googlecode/mapperdao/ManyToOneExternalEntitySuite.scala)

[one to many](https://code.google.com/p/mapperdao/source/browse/src/test/scala/com/googlecode/mapperdao/OneToManyExternalEntitySuite.scala)

[one to one reverse](https://code.google.com/p/mapperdao/source/browse/src/test/scala/com/googlecode/mapperdao/OneToOneReverseExternalEntitySuite.scala)

### Querying ###

Joining with external entities works only if the external entity is stored in a table into the database and also all joined columns are mapped using the normal mapperdao mappings , i.e. val name=column("name") to (_.name) . In that case, QueryDao will do the joins as it would happen if the entity was a mapperdao entity._

### Transactions ###

If mapperdao's transaction manager or springframework transactions are used and the external entity is stored in the database, then all operations will be done within a transaction.

