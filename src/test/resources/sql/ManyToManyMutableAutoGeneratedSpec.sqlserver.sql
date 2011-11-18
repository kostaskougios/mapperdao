[ddl]
create table Product (
	id int not null identity(1,1),
	name varchar(100) not null,
	primary key(id)
)
;
create table Attribute (
	id int not null identity(1,1),
	name varchar(100) not null,
	value varchar(100) not null,
	primary key(id)
)
;
create table Product_Attribute (
	product_id int not null,
	attribute_id int not null,
	primary key(product_id,attribute_id),
	foreign key(product_id) references Product(id) on delete cascade,
	foreign key(attribute_id) references Attribute(id) on delete cascade
)
