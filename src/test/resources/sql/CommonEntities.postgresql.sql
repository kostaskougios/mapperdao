[person-company]
create table Company(
	id serial not null,
	name varchar(40) not null,
	primary key (id)
)
;
create table Person(
	id serial not null,
	name varchar(40) not null,
	company_id int not null,
	primary key (id),
	foreign key (company_id) references Company(id) on delete cascade on update cascade
)
