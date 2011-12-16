[ddl]
create table Product (
	id int not null,
	name varchar(100) not null,
	primary key(id)
)
;
create table Attribute (
	id int not null,
	name varchar(100) not null,
	value varchar(100) not null,
	primary key(id)
)
;
create table Product_Attribute (
	product_id int not null,
	attribute_id int not null,
	primary key(product_id,attribute_id),
	foreign key (product_id) references Product(id) on delete cascade,
	foreign key (attribute_id) references Attribute(id) on delete cascade
)
;
create or replace trigger cascade_update_Product
after update of id on Product
for each row
begin
	update Product_Attribute
	set product_id = :new.id
	where product_id = :old.id;
end;
;
create or replace trigger cascade_update_Attribute
after update of id on Attribute
for each row
begin
	update Product_Attribute
	set attribute_id = :new.id
	where attribute_id = :old.id;
end;
