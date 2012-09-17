[person-company]
create table Company(
	id serial not null,
	name varchar(40) not null,
	primary key (id)
) engine InnoDB
;
create table Person(
	id serial not null,
	name varchar(40) not null,
	company_id bigint unsigned not null,
	primary key (id),
	foreign key (company_id) references Company(id) on delete cascade on update cascade
) engine InnoDB

[husband-wife]
create table Wife(
	id serial not null,
	name varchar(20) not null,
	age int not null,
	primary key (id)
)
;
create table Husband(
	name varchar(20) not null,
	age int not null,
	wife_id bigint unsigned not null,
	constraint FK_Husband_Wife foreign key (wife_id) references Wife(id)
)

[image]
create table Image(
	id serial primary key,
	name varchar(20) not null,
	data blob not null
)