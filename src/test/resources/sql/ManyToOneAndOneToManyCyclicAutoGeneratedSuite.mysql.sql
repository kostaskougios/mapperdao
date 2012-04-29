[ddl]
create table Company (
	id serial not null,
	name varchar(100) not null,
	primary key(id)
)
;
create table Person (
	id serial not null,
	name varchar(100) not null,
	company_id bigint unsigned,
	primary key(id),
	foreign key (company_id) references Company(id) on delete cascade
)
