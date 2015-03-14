There is nothing special on mapping enumerations. The enumerations can be converted back and forth to any type. The following example stores the Gender of a Customer using a char representation:

```
object Gender extends Enumeration {
	type Gender = Value
	val Male, Female = Value

	def toString(gender: Gender) = gender match {
		case Male => "M"
		case Female => "F"
	}

	def fromString(g: String): Gender = g match {
		case "M" => Male
		case "F" => Female
	}
}

...

object CustomerEntity extends Entity[Int,SurrogateIntId, Customer] {
	// the Gender is stored as String in the db but it is modeled as an Enumeration 
	val gender = column("gender") to (customer => Gender.toString(customer.gender))
...

def constructor(implicit m) = {
	val g = Gender.fromString(m(gender))
	new Customer(..., g) with Stored ...
}
```

The interesting bit is the following:

```
val gender = column("gender") to (customer => Gender.toString(customer.gender))
```

The enumeration is converted to a string (and stored as 1 char string in the database)

And then back from string to enumeration:

```
val g = Gender.fromString(m(gender))
```
