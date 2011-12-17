[one-to-many]
create table Product (
	id int not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	title varchar(100) not null,
	primary key(id)
)
;
create table Price (
	currency varchar(3) not null,
	unitprice decimal(6,3),
	saleprice decimal(6,3),
	product_id int not null,
	primary key (product_id,currency,unitprice),
	foreign key (product_id) references Product(id)
)
;
