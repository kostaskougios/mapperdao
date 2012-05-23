[ddl]
create table Product (
	id int not null,
	name varchar(100) not null,
	primary key(id)
)
;
create table Attribute (
	id int not null,
	name varchar(100) not null,
	value varchar(100) not null,
	primary key(id)
)
;
create table Product_Attribute (
	product_id int not null,
	attribute_id int not null,
	primary key(product_id,attribute_id),
	foreign key (product_id) references Product(id) on update cascade on delete cascade,
	foreign key (attribute_id) references Attribute(id) on update cascade on delete cascade
)
;
create table Property (
	id int not null,
	name varchar(100) not null,
	value varchar(100) not null,
	primary key(id)
) 
;
create table Product_Property (
	product_id int not null,
	property_id int not null,
	primary key(product_id,property_id),
	foreign key (product_id) references Product(id) on update cascade on delete cascade,
	foreign key (property_id) references Property(id) on update cascade on delete cascade
)
