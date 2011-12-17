[ddl]
create table Company (
	id int not null,
	name varchar(100) not null,
	primary key(id)
) engine InnoDB
;
create table Person (
	id int not null,
	name varchar(100) not null,
	company_id int,
	primary key(id),
	foreign key (company_id) references Company(id) on delete cascade on update cascade
) engine InnoDB
