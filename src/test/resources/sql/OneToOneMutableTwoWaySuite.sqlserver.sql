[ddl]
create table Product (
	id int not null,
	primary key (id)
)
;
create table Inventory (
	product_id int not null,
	stock int not null,
	primary key (product_id),
	foreign key (product_id) references Product(id) on delete cascade on update cascade 
)
;

