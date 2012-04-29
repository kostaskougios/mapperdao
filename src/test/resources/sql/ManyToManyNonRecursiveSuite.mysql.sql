[ddl]
create table Product (
	id serial not null,
	name varchar(100) not null,
	primary key(id)
)
;
create table Attribute (
	id serial not null,
	name varchar(100) not null,
	value varchar(100) not null,
	primary key(id)
)
;
create table Product_Attribute (
	product_id bigint unsigned not null,
	attribute_id bigint unsigned not null,
	primary key(product_id,attribute_id)
)
