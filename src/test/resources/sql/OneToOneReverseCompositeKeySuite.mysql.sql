[ddl]
create table Product (
	id serial not null,
	name varchar(50) not null,
	primary key (id)
)
;
create table Inventory (
	id int not null,
	refCode varchar(20) not null,
	product_id bigint unsigned not null,
	stock int not null,
	primary key (id,refCode),
	foreign key (product_id) references Product(id) on delete cascade 
)
