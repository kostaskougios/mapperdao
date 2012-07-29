[ddl]
create table Product (
	id serial not null,
	refCode varchar(20) not null,
	primary key (id,refCode)
)
;
create table Inventory (
	id serial not null,
	product_id int not null,
	product_refCode varchar(20) not null,
	stock int not null,
	primary key (id),
	foreign key (product_id,product_refCode) references Product(id,refCode) on delete cascade on update cascade
)
