[ddl]
create table Person (
	id serial not null,
	name varchar(100) not null,
	primary key (id)
)
;
create table House (
	address varchar(100) not null,
	person_id int not null,
	primary key (address,person_id),
	constraint FK_House_Person foreign key (person_id) references Person(id)
		on delete cascade on update cascade
)
