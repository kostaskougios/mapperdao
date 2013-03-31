[ddl]

create schema test
;

create table test.Product (
	id serial not null,
	name varchar(100) not null,
	primary key (id)
)
;
create table test.Attribute (
	id serial not null,
	name varchar(100) not null,
	value varchar(100) not null,
	primary key(id)
)
;

create table test.Product_Attribute (
	product_id int not null,
	attribute_id int not null,
	primary key (product_id,attribute_id),
	foreign key (product_id) references test.Product(id) on delete cascade on update cascade,
	foreign key (attribute_id) references test.Attribute(id) on delete cascade on update cascade
)
