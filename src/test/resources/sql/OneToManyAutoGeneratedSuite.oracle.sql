[cascade]
create table Person (
	id int not null,
	name varchar(100) not null,
	surname varchar(100) not null,
	age int not null,
	primary key (id)
)
;
create table JobPosition (
	id int not null,
	name varchar(100) not null,
	rank int not null,
	person_id int not null,
	primary key (id),
	constraint FK_JobPosition_Person foreign key (person_id) references Person(id)
	on delete cascade
)
;
create table House (
	id int not null,
	address varchar(100) not null,
	person_id int not null,
	primary key (id),
	constraint FK_House_Person foreign key (person_id) references Person(id)
	on delete cascade
)

[nocascade]

create table Person (
	id int not null,
	name varchar(100) not null,
	surname varchar(100) not null,
	age int not null,
	primary key (id)
)
;
create table JobPosition (
	id int not null,
	name varchar(100) not null,
	rank int not null,
	person_id int,
	primary key (id),
	constraint FK_JobPosition_Person foreign key (person_id) references Person(id)
	on delete set null
)
;
create table House (
	id int not null,
	address varchar(100) not null,
	person_id int not null,
	primary key (id),
	constraint FK_House_Person foreign key (person_id) references Person(id)
	on delete cascade
)