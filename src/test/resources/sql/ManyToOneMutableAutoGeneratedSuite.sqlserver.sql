[ddl]
create table Company (
	id int not null identity(1,1),
	name varchar(100) not null,
	primary key(id)
)
;
create table House (
	id int not null identity(1,1),
	address varchar(100) not null,
	primary key(id)
)
;
create table Person (
	id int not null identity(1,1),
	name varchar(100) not null,
	company_id int,
	house_id int,
	primary key(id),
	foreign key (company_id) references Company(id) on delete cascade,
	foreign key (house_id) references House(id) on delete cascade
)
