[ddl]
create table Person (
	id serial not null,
	name varchar(100) not null,
	primary key (id)
)
;
create table PostCode (
	id serial not null,
	code varchar(7),
	primary key (id)
)
;
create table House (
	address varchar(100) not null,
	person_id int not null,
	postcode_id int not null,
	primary key (address,person_id),
	constraint FK_House_Person foreign key (person_id) references Person(id)
		on delete cascade on update cascade,
	constraint FK_Houser_PostCode foreign key (postcode_id) references PostCode(id)
		on delete cascade on update cascade
)
