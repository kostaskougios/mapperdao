[string-based]
create table Product (
	id int not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	name varchar(100) not null,
	primary key (id)
)
;

create table Category (
	id int not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	name varchar(50) not null,
	primary key (id)
)
;

create table Product_Category (
	product_id int not null,
	category_id int not null,
	primary key (product_id,category_id),
	foreign key (product_id) references Product(id) on delete cascade,
	foreign key (category_id) references Category(id) on delete cascade
)