[ddl]
create table Company (
	id int not null AUTO_INCREMENT,
	name varchar(255) not null,
	constraint pk_company primary key (id)
)
;

create table Computer (
	id int not null AUTO_INCREMENT,
	name varchar(255) not null,
	company_id int,
	constraint pk_computer primary key (id),
	constraint fk_computer_company foreign key (company_id) references Company(id) on delete cascade
)
;

