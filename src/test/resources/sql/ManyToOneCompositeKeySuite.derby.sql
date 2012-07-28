[ddl]
create table City (
	id bigint not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	reference varchar(10) not null,
	name varchar(20) not null,
	primary key (id,reference)
)
;
create table House (
	id bigint not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	city_id bigint not null,
	city_reference varchar(10) not null,
	address varchar(50) not null,
	primary key (id),
	constraint FK_House_City foreign key (city_id,city_reference) 
		references City(id,reference) on delete cascade
)