[ddl]
create table Product (
	id serial not null,
	name varchar(100) not null,
	primary key (id)
)
;
create table Property (
	name varchar(100) not null,
	value varchar(100) not null,
	product_id int not null,
	primary key (product_id,name),
	foreign key (product_id) references Product(id) on delete cascade on update cascade
)
