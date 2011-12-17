[ddl]
create table Product (
	id int not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	name varchar(100) not null,
	primary key(id)
)
;
create table Attribute (
	id int not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	name varchar(100) not null,
	value varchar(100) not null,
	primary key(id)
)
;
create table Product_Attribute (
	product_id int not null,
	attribute_id int not null,
	primary key(product_id,attribute_id)
)
