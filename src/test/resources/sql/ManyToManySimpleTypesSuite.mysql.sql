[string-based]
create table Product (
	id serial not null,
	name varchar(100) not null,
	primary key (id)
)
;

create table Category (
	id serial not null,
	name varchar(50) not null,
	primary key (id)
)
;

create table Product_Category (
	product_id bigint unsigned not null,
	category_id bigint unsigned not null,
	primary key (product_id,category_id),
	foreign key (product_id) references Product(id) on delete cascade,
	foreign key (category_id) references Category(id) on delete cascade
)