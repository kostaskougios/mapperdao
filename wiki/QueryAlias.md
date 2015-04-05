"Entity as 'X" can be used to alias a table. Then the alias can be used in join & where expressions , examples:

```
select from (pe as 'x)
join(pe as 'x, pe.lives, he as 'y)
join(he as 'y, he.address, ad as 'z)
where ('z, ad.postCode) === "SE1 1AA"
```

```
(select from p
	join(p, p.attributes, attr)
	join(p, p.attributes, attr as 'a1)
	join(p, p.attributes, attr as 'a2)
	where (attr.name === "size" and attr.value === "46'")
	and (('a1, attr.name) === "colour" and ('a1, attr.value) === "white")
	and (('a2, attr.name) === "dimensions" and ('a2, attr.value) === "100x100")
)
```