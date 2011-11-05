[create-tables]
create table Product (
	id serial not null,
	name varchar(100) not null,
	primary key (id)
)
;
create table ProductTags (
	product_id int not null,
	tag varchar(30) not null,
	foreign key (product_id) references Product(id) on delete cascade
)