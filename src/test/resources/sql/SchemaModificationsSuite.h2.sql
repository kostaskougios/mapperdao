[person]
create table tmp_Person (
	id serial primary key,
	name varchar(20) not null
)

[product-attribute]
create table tmp_Product (
	id serial not null,
	name nvarchar(100) not null,
	primary key (id)
)
;
create table tmp_Attribute (
	id serial not null,
	name nvarchar(100) not null,
	value nvarchar(100) not null,
	primary key(id)
)
;

create table tmp_Product_Attribute (
	product_id int not null,
	attribute_id int not null,
	primary key (product_id,attribute_id),
	foreign key (product_id) references tmp_Product(id) on delete cascade on update cascade,
	foreign key (attribute_id) references tmp_Attribute(id) on delete cascade on update cascade
)
