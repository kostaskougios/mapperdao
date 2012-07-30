[ddl]
create table Product (
	id int not null identity(1,1),
	name varchar(50) not null,
	primary key (id)
)
;
create table Inventory (
	id int not null,
	refCode varchar(20) not null,
	product_id int not null,
	stock int not null,
	primary key (id,refCode),
	foreign key (product_id) references Product(id) on delete cascade on update cascade
)
