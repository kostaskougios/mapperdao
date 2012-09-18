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

[husband-wife]
create table Wife(
	id int not null,
	name varchar(20) not null,
	age int not null,
	primary key (id)
)
;
create table Husband(
	name varchar(20) not null,
	age int not null,
	wife_id int not null,
	constraint FK_Husband_Wife foreign key (wife_id) references Wife(id)
)

[image]
create table Image(
	id int primary key,
	name varchar(20) not null,
	data blob not null
)