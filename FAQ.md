Creating mappings might seem a bit verbose but in the long run they offer a lot more benefits compared i.e. to annotating your domain classes:

1. domain classes are clean from ORM code

2. mapperdao mappings offer a way to refer to the columns when writing queries, i.e. (select from e where e.name==='x').toList . Compared to hibernate queries, this is typesafe, easier to read and less typing.

3. mappings also allows manipulating the data before persisting them, something not possible when using other ORM's: val name=column("name) to (_.name.toLowerCase)_

4. the constructor method allows control over constructing persisted classes, i.e. we can order the items of a list:

def constructor(implicit m) = new Person(name,houses.orderWith(_.name<_.name))

5. sometimes JPA is more verbose than mapperdao and even worse, it pollutes the domain model:

```
@ManyToOne( cascade = {CascadeType.PERSIST, CascadeType.MERGE} )
    @JoinTable(name="Flight_Company",
        joinColumns = @JoinColumn(name="FLIGHT_ID"),
        inverseJoinColumns = @JoinColumn(name="COMP_ID")
    )
```

  * **does mapperdao support Java based domain classes**

Yes, mapperdao support Java based domain classes. The mappings ofcourse have to be in Scala.

  * **can mapperdao work with my existing hibernate-based project?**

Yes. MapperDao has the concept of "[External Entities](ExternalEntities.md)". With external entities mapperdao can work with existing legacy dao's. Please note that though mapperdao can work with i.e. hibernate entities, the other way around is not possible as hibernate can't view mapperdao mappings.