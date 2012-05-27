[product-attribute]
create table Product (
	id serial not null,
	name nvarchar(100) not null,
	primary key (id)
)
;
create table Attribute (
	id serial not null,
	name nvarchar(100) not null,
	value nvarchar(100) not null,
	primary key(id)
)
;

create table Product_Attribute (
	product_id int not null,
	attribute_id int not null,
	primary key (product_id,attribute_id),
	foreign key (product_id) references Product(id) on delete cascade on update cascade,
	foreign key (attribute_id) references Attribute(id) on delete cascade on update cascade
)
