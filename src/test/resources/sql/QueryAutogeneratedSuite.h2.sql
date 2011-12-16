[ddl]
create table company (
	id serial not null,
	name varchar(255) not null,
	constraint pk_company primary key (id)
)
;

create table computer (
	id serial not null,
	name varchar(255) not null,
	company_id int,
	constraint pk_computer primary key (id),
	constraint fk_computer_company foreign key (company_id) references company(id) on delete cascade
)
;

