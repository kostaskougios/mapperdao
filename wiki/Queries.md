# Queries #

Querying with mapper dao has minimal learning curve because the query DSL resembles casual select statements.

Assuming you've already [setup a queryDao](SetupDaos.md), you are ready to execute queries.

Here are our sample domain classes and entities:
```
class Customer(val firstName: String, val lastName: String, ...)
class Order(val date: DateTime, val customer: Customer, val totalAmount: BigDecimal, ...)
...


object CustomerEntity extends Entity[Int,SurrogateIntId, Customer]("customers") {
	val firstname = column("firstname") to (_.firstName)
	val lastname = column("lastname") to (_.lastName)
...
}

object OrderEntity extends Entity[Int,SurrogateIntId, Order]("orders") {
	val date = column("orderdate") to (_.date)
	val customer = manytoone(CustomerEntity) foreignkey "customerid" to (_.customer)
	val totalAmount = column("totalamount") to (_.totalAmount)

...
}

```

## Simple query ##
Lets start by obtaining an "alias" for our entities. An alias is just a reference to our entity objects.

```
val o = OrderEntity
val c = CustomerEntity
```

We could write our queries without the aliases, but the aliases make the queries easier to read:

```
// import the query dsl
import Query._

/**
 * all orders that the totalamount is between min and max
 */
def byTotal(min: Double, max: Double): List[Order with SurrogateIntId] = {
	val q=select from o where o.totalAmount >= min and o.totalAmount <= max
	queryDao.query(q)
}
```

Or simply:

```
import Query._
import queryDao._

def byTotal(min: Double, max: Double) = 
	query(select from o where o.totalAmount >= min and o.totalAmount <= max)

```

Alternatively, if there is an implicit queryDao available in scope, then it can be written like this:

```
import Query._

def byTotal(min: Double, max: Double) = 
	(select from o where o.totalAmount >= min and o.totalAmount <= max).toList

```


## Join ##

In case we would like to get all orders for a specific customer, we could do something like this:

```
import Query._
import queryDao._

val o = OrderEntity
val c = CustomerEntity

def byCustomer(lastname: String) = 
	query(select from o join (o, o.customer, c) where c.lastname === lastname)
```

Please notice the join:
```
join (o, o.customer, c)
```

It is a join from o (OrderEntity) for the o.customer relationship (which is a many-to-one) to the customer entity.
It practically joins using the following relationship:

```
val customer = manytoone(CustomerEntity) foreignkey "customerid" to (_.customer)
```

and will create a query like

```
select ... from Orders o join Customers c on o.customerid=c.id 
where c.lastname = ?
```

## Spliting queries into multiple lines ##

Scala allows an expression to be split into multiple lines, provided the expression is within parenthesis. This way, a query
can be split into multiple lines i.e.

```
val myQuery = (
	select from p
	join (p, p.owns, h)
	where (h.address === "Madrid" or h.address === "Rome")
	and h.id >= 6
)
```
