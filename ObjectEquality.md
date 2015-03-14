MapperDao doesn't use equals() or hashCode() when comparing objects or collections.

For entities, mapperdao compares all mapped primitive fields (Int,String etc).
MapperDao, via ValuesMap, keeps track of all the original and changed values for each entity.  Mappings help convert an entity instance into a ValuesMap.
This way mapperdao knows which columns to compare, insert update without using entity.equals() or entity.hashCode() or reflection.

For many-to-many and one-to-many, and since those by definition contain collections, and for non-persisted objects,
mapperdao uses object.eq() (reference equality) to decide which objects were added, were already contained (intersection) or removed.

So when updating, existing collections must be transformed to new ones:

For immutable entities:

```
val product = Product("blue jean", Set(Attribute("colour", "blue", Set()), Attribute("size", "medium", Set()), Attribute("size", "large", Set())))
val inserted = mapperDao.insert(ProductEntity, product)

// modify the attributes collection
val changed = Product("just jean", inserted.attributes.filterNot(_.name == "size"));
val updated = mapperDao.update(ProductEntity, inserted, changed)
```

For mutable entities:
```
val inserted = mapperDao.insert(ProductEntity, Product("blue jean", Set(Attribute("colour", "blue", Set()))))

inserted.attributes += Attribute("size", "medium", Set())
// add/remove items
val updated = mapperDao.update(ProductEntity, inserted)

```