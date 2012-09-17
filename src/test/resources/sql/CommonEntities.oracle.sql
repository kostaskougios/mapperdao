[person-company]
create table Company(
	id int not null,
	name varchar(40) not null,
	primary key (id)
)
;
create table Person(
	id int primary key,
	name varchar(40) not null,
	company_id int not null,
	foreign key (company_id) references Company(id) on delete cascade
)

[image]
create table Image(
	id int primary key,
	name varchar(20) not null,
	data blob not null
)