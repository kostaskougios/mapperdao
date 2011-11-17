[ddl]
create table Product (
	id int not null,
	primary key (id)
)
;
create table Inventory (
	product_id int not null,
	stock int not null,
	primary key (product_id),
	foreign key (product_id) references Product(id) on delete cascade 
)
;
create or replace trigger cascade_update
after update of id on Product
for each row
begin
	update Inventory
	set product_id = :new.id
	where product_id = :old.id;
end;
