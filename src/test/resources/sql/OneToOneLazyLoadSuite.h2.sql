[ddl]
create table Product (
	id serial not null,
	x int not null,
	primary key (id)
)
;
create table Inventory (
	product_id bigint not null,
	stock int not null,
	primary key (product_id),
	foreign key (product_id) references Product(id) on delete cascade
)
