[ddl]
create table Product (
	id int not null,
	name varchar(100) not null,
	primary key(id)
)
;
create table Info (
	title varchar(100) not null,
	product_id int not null,
	location_id int not null,
	foreign key (product_id) references Product(id) on delete cascade on update cascade
)