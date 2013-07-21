[ddl]
create table catalog (
	id int primary key
)
;
create table product (
	id int primary key,
	catalog_id int not null,
	constraint fk_catalog foreign key(catalog_id) references catalog(id)
)
;
create table inventory (
	product_id int primary key,
	stock int not null,
	constraint fk_inventory foreign key(product_id) references product(id)
)

