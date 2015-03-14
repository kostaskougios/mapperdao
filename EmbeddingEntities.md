Embedding entities (entities part of an other entity that are stored in the same table like their parent entity) comes naturally with mapperdao.

The "dellstore" example contains embedded entities (Customer class which contains an Address and CreditCard embedded entities),
please download it from https://code.google.com/p/mapperdao-examples/

## Domain Classes ##

Lets assume that a customer has a credit card and we want to store the information of the credit card into the customer table.

```
case class CreditCard(val cardType: Int, val card: String, val expiration: String)

class Customer(val firstName: String, val lastName: String, val creditCard: CreditCard)
```

## Mapping ##

There will be only 1 mapping, the CustomerEntity which maps customers (and their cards) to the tables.

```
object CustomerEntity extends Entity[Int,SurrogateIntId, Customer]("customers") {
	val firstname = column("firstname") to (_.firstName)
	val lastname = column("lastname") to (_.lastName)
	// embedded CreditCard class
	val creditcardtype = column("creditcardtype") to (_.creditCard.cardType)
	val creditcard = column("creditcard") to (_.creditCard.card)
	val creditcardexpiration = column("creditcardexpiration") to (_.creditCard.expiration)
	// end of CreditCard config

	// constructor
	def constructor(implicit m) = {
		// instantiate the embedded entities
		val creditCard = CreditCard(creditcardtype, creditcard, creditcardexpiration)
		new Customer(firstname, lastname,creditCard) with Stored
	}
}
```

The interesting bit is this:

```
val creditcardtype = column("creditcardtype") to (_.creditCard.cardType)
val creditcard = column("creditcard") to (_.creditCard.card)
val creditcardexpiration = column("creditcardexpiration") to (_.creditCard.expiration)
```

The embedded entity is mapped to the appropriate columns, i.e. creditcardtype is mapped to creditcardtype column and the value is `_.creditCard.cardType`

Finally, when constructing the Customer entity, we first need to create a credit card instance:

```
def constructor(implicit m) = {
	val creditCard = CreditCard(creditcardtype, creditcard, creditcardexpiration)
	new Customer(firstname, lastname,creditCard) with Stored
}
```