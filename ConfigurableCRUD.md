## Selects ##

Selects can be fine tuned using the SelectConfig class. Only some of an entities relationships can be retrieved from the database, improving performance.

```
val product = mapperDao.select(SelectConfig(skip = Set(ProductEntity.attributes)), ProductEntity, 5).get
```

The above call will load Product entity with id 5 from the database, along with all it's relationships but not the attributes. `product.attributes` will be an empty Traversable.

## Deletes ##

By default, mapperdao relies on `on cascade delete` constrains to propagate deletes to related data. This is the most efficient and natural way of cascading deletes. But mapperdao can also propagate the deletes by deleting each related entity:

```
val inserted = mapperDao.insert(ProductEntity, Product("blue jean", Set(Attribute("colour", "blue", Set()), Attribute("size", "medium", Set()))))
mapperDao.delete(DeleteConfig(true), ProductEntity, inserted)
```

In the above many-to-many example, mapperdao will propagate the deletes to Product\_Attribute table (but not to Attribute table since this is a many-to-many relationship).

Propagation of deletes can be further configured by skipping propagation for some relationships:

```
mapperDao.delete(DeleteConfig(true, skip = Set(ProductEntity.attributes)), ProductEntity, inserted)
```

In the above example, deletes propagate except for `product.attributes`. Propagation depends on the relationship between entities:

  * for many-to-many. the propagation occurs only to the intermediate table, i.e. for a `Product->Product_Attribute->Attribute` relationship, mapperdao will propagate the delete only to `Product_Attribute`
  * for many-to-one, propagation is not necessary
  * for one-to-many, propagation deletes rows from the related entity (the 'many' part)
  * for one-to-one, propagation deletes rows from the related entity

## Updates ##

Currently configuring updates is focused on the deleted entities of a one-to-many relationship. Typically there will be a foreign key constraint and a on-delete-cascade but if for any reason this is not possible, mapperdao can cascade the delete operations that must occur when updating an entity that contains a one-to-many relationship.

Lets take the following database schema for example:

```
create table Person (
	id int not null,
	name varchar(100) not null,
	primary key (id)
)
;
create table House (
	id int not null,
	person_id int not null,
	primary key (id),
	constraint FK_House_Person foreign key (person_id) references Person(id)
)
;

create table Floor (
	id int not null,
	description varchar(100) not null,
	house_id int not null,
	primary key (id),
	constraint FK_Floor_House foreign key (house_id) references House(id)
)
```

The problem here is that if we update the person entity, removing a house, then the house row is deleted but not the floor rows that correspond to that house.

Lets review the entities and mappings. A Person has one or more houses and each house has a number of floors:

```
case class Person(val id: Int, var name: String, owns: Set[House])
case class House(val id: Int, val floors: Set[Floor])
case class Floor(val id: Int, val description: String)

object FloorEntity extends Entity[Int,NaturalIntId,Floor] {
	val id = key("id") to (_.id)
	val description = column("description") to (_.description)

	def constructor(implicit m) = new Floor(id, description) with Stored
}

object HouseEntity extends Entity[Int,NaturalIntId,House] {
	val id = key("id") to (_.id)
	val floors = onetomany(FloorEntity) to (_.floors)

	def constructor(implicit m) = new House(id, floors) with Stored
}

object PersonEntity extends Entity[Int,NaturalIntId,Person] {
	val id = key("id") to (_.id)
	val name = column("name") to (_.name)
	val houses = onetomany(HouseEntity) to (_.owns)
	def constructor(implicit m) = new Person(id, name, houses) with Stored
}
```

If we would like to update a person and remove 1 of the houses he owns, then we could instruct mapperdao to propagate the delete to the floors:

```
val inserted = mapperDao.insert(PersonEntity, Person(1, "kostas", Set(House(10, Set(Floor(5, "nice floor"), Floor(6, "top floor"))), House(11, Set(Floor(7, "nice floor"), Floor(8, "top floor"))))))
mapperDao.update(UpdateConfig(deleteConfig = DeleteConfig(propagate = true)), PersonEntity, inserted, Person(inserted.id, inserted.name, inserted.owns.filterNot(_.id == 11)))
```

[example](https://code.google.com/p/mapperdao/source/browse/src/test/java/com/googlecode/mapperdao/UpdateConfigSuite.scala)
