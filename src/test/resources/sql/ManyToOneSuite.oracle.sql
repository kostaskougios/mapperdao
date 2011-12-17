[ddl]
create table Company (
	id int not null,
	name varchar(100) not null,
	primary key(id)
)
;
create table House (
	id int not null,
	address varchar(100) not null,
	primary key(id)
)
;
create table Person (
	id int not null,
	name varchar(100) not null,
	company_id int,
	house_id int,
	primary key(id),
	foreign key (company_id) references Company(id) on delete cascade,
	foreign key (house_id) references House(id) on delete cascade
)
;
create or replace trigger cascade_update
after update of id on House
for each row
begin
	update Person
	set house_id = :new.id
	where house_id = :old.id;
end;
