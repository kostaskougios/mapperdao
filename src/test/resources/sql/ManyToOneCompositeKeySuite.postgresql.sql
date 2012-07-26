[ddl]
create table City (
	id serial not null,
	reference varchar(10) not null,
	name varchar(20) not null,
	primary key (id,reference)
)
;
create table House (
	id serial not null,
	city_id bigint not null,
	city_reference varchar(10) not null,
	address varchar(50) not null,
	primary key (id),
	constraint FK_House_City foreign key (city_id,city_reference) 
		references City(id,reference) on delete cascade
)