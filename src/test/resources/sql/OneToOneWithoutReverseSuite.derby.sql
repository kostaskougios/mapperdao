[ddl]
create table Product (
	id int not null,
	primary key (id)
)

;

create table Inventory (
	id int not null,
	product_id int,
	stock int not null,
	primary key (id),
	foreign key (product_id) references Product(id) on delete cascade on update restrict
)
