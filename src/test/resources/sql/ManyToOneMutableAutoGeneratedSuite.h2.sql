[ddl]
create table Company (
	id serial not null,
	name varchar(100) not null,
	primary key(id)
)
;
create table House (
	id serial not null,
	address varchar(100) not null,
	primary key(id)
)
;
create table Person (
	id serial not null,
	name varchar(100) not null,
	company_id int,
	house_id int,
	primary key(id),
	foreign key (company_id) references Company(id) on delete cascade,
	foreign key (house_id) references House(id) on delete cascade
)
