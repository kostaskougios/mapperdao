[ddl]
create table Person (
	id serial not null,
	name varchar(100) not null,
	primary key (id)
)
;
create table House (
	id serial not null,
	address varchar(100) not null,
	person_id int not null,
	primary key (id),
	constraint FK_House_Person foreign key (person_id) references Person(id)
		on delete cascade on update cascade
)
;
create table Car (
	id serial not null,
	model varchar(50) not null,
	person_id int not null,
	primary key (id),
	constraint FK_Car_Person foreign key (person_id) references Person(id)
		on delete cascade on update cascade
)
