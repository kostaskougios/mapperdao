[ddl]
create table Employee (
	no varchar(20) not null,
	primary key (no)
)
;
create table Company (
	no varchar(20) not null,
	name varchar(20) not null,
	primary key (no)
)
;
create table WorkedAt (
	employee_no varchar(20) not null,
	company_no varchar(20) not null,
	year int not null,
	primary key (employee_no,company_no),
	foreign key (employee_no) references Employee(no) on delete cascade,
	foreign key (company_no) references Company(no) on delete cascade
)
