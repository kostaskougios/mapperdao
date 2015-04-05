## Domain Classes ##

Our sample domain consists of people working for a company. We will map the many-to-one relationship between Person and Company.

For this example, we won't map the one-to-many relationship between Company and Person. (Please see [one to many](OneToManyMappings.md) wiki, which describes such mapping)

Here are our domain classes, which are case classes so that equals and hashCode are implemented for us. (we'll use case classes to simplify the examples, but it is not necessary. Providing proper equals() and hashCode() implementation, would also do)

```
case class Person(val id: Int, val name: String, val company: Company)
case class Company(val id: Int, val name: String)
```

Assuming that we got mapperDao configured ( [SetupDaos](SetupDaos.md) ), our tables (Person,Company) & mappings (PersonEntity,CompanyEntity) ready, we can use mapperdao to persist, update, select, delete and query for our entities:

```
import com.googlecode.mapperdao._

// configure mapperDao and queryDao as described at https://code.google.com/p/mapperdao/wiki/SetupDaos ...

...

// and now create & persist entities

val company1 = insert(CompanyEntity, Company(5, "Coders limited"))
val company2 = insert(CompanyEntity, Company(6, "Scala Inc"))
val person = Person(2, "Kostas", company1)

val inserted = insert(PersonEntity, person)

val modified = Person(2, "changed", company2)
// when updating immutable entities, use update(entity,oldValue,newValue). You will need to provide both the old value of the entity and a new instance with the modifications
val updated = update(PersonEntity, inserted, modified)

val selected = select(PersonEntity, 2).get
println(selected)

mapperDao.delete(PersonEntity, selected)

```

## Tables ##

The table ddl (for postgresql) follows:

```
create table Company (
	id serial not null,
	name varchar(100) not null,
	primary key(id)
)
create table Person (
	id serial not null,
	name varchar(100) not null,
	company_id int,
	primary key(id),
	foreign key (company_id) references Company(id) on delete cascade,
)

```

## Mappings ##

We are now ready to map the domain classes to the database tables.

```
object PersonEntity extends Entity[Int,NaturalIntId,Person] {
	val id = key("id") to (_.id)
	val name = column("name") to (_.name)
	val company = manytoone(CompanyEntity) to (_.company)

	def constructor(implicit m) = new Person(id, name, company) with Stored
}

object CompanyEntity extends Entity[Int,NaturalIntId,Company] {
	val id = key("id") to (_.id)
	val name = column("name") to (_.name)

	def constructor(implicit m) = new Company(id, name) with Stored
}

```

The interesting bit of code is

```
val company = manytoone(CompanyEntity) to (_.company)
```

This takes care of the many-to-one mapping between Person and Company. Person.company\_id is the FK to Company table.
The Foreign Entity is `CompanyEntity` and the company instance is retrieved via the `_.company` function.
