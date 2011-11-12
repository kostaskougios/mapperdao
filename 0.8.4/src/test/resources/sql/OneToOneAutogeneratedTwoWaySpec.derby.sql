[cascade]
create table Product (
	id int not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
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
	id int not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	x int not null,
	primary key (id)
)
;
create table Inventory (
	product_id int,
	stock int not null,
	foreign key (product_id) references Product(id) on delete set null
)
