[ddl]
create table City (
	id int not null,
	reference varchar(10) not null,
	name varchar(20) not null,
	primary key (id,reference)
)
;
create table House (
	id int not null,
	city_id int not null,
	city_reference varchar(10) not null,
	address varchar(50) not null,
	primary key (id),
	constraint FK_House_City foreign key (city_id,city_reference) 
		references City(id,reference) on delete cascade
)