[cascade]
create table Product (
	id int not null,
	x int not null,
	primary key (id)
)
;
create table Inventory (
	product_id int not null,
	stock int not null,
	primary key (product_id),
	foreign key (product_id) references Product(id) on delete cascade
)

[nocascade]
create table Product (
	id int not null,
	x int not null,
	primary key (id)
)
;
create table Inventory (
	product_id int,
	stock int not null,
	primary key (product_id),
	foreign key (product_id) references Product(id) on delete set null
)
