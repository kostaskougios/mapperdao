[OneToManyDecl]
create table Person (
	id int not null,
	name varchar(100) not null,
	primary key (id)
)
;
create table House (
	id int not null,
	person_id int not null,
	primary key (id),
	constraint FK_House_Person foreign key (person_id) references Person(id)
)
;

create table Floor (
	id int not null,
	description varchar(100) not null,
	house_id int not null,
	primary key (id),
	constraint FK_Floor_House foreign key (house_id) references House(id)
)
