## Domain Model ##

Lets map a Product and an Inventory class. A product has 1 inventory and 1 inventory belongs to one product.
In this example we will map both product=>inventory and inventory=>product but also only 1 of those could be mapped.

Lets view our domain classes:

```
class Inventory(var product: Product, var stock: Int)
class Product(val id: Int, var inventory: Inventory)
```

After mapping, we would be able to do things like:

```
val product = Product(1, Inventory(null, 5))
product.inventory.product = product
val inserted = insert(ProductEntity, product)
inserted.inventory.stock = 8
val updated = update(ProductEntity, inserted)
// updated == inserted
val selected = select(ProductEntity, updated.id).get
// selected == updated

delete(ProductEntity, selected)
```

and run queries like:

```
val p = ProductEntity
val i = InventoryEntity
import Query._

val list1=query(select from p join (p, p.inventory, i) where i.stock > 5)
val list2=query(select from p join (p, p.inventory, i) where
		i.stock > 5 and i.stock < 13)
```

## Tables ##

Here is the ddl for the tables for postgresql:

```
create table Product (
	id int not null,
	primary key (id)
)

create table Inventory (
	product_id int not null,
	stock int not null,
	sold int not null,
	primary key (product_id),
	foreign key (product_id) references Product(id) on delete cascade
)
```

## Mappings ##

The mappings:

```
object InventoryEntity extends Entity[Unit,NoId,Inventory] {
	val product = onetoone(ProductEntity) to (_.product)
	val stock = column("stock") to (_.stock)

	def constructor(implicit m) = new Inventory(product, stock) with Stored
}

object ProductEntity extends Entity[Int,NaturalIntId,Product] {
	val id = key("id") to (_.id)
	val inventory = onetoonereverse(InventoryEntity) to (_.inventory)

	def constructor(implicit m) = new Product(id, inventory) with Stored
}
```

The interesting piece of code is

```
val product = onetoone(ProductEntity) to (_.product)
...
val inventory = onetoonereverse(InventoryEntity) to (_.inventory)
```

So Product has a oneToOne(reverse) relationship with `InventoryEntity` via the `Inventory.product_id` column.
And inventory has a one-to-one relationship with `ProductEntity` via the same column.

The method onetoone should be used on the entity which table contains the FK column, i.e. `Inventory.product_id`.

The method onetoonereverse should be used for the reverse mapping.
