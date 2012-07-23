[ddl]
create table UserX (
	id int not null,
	name varchar(50) not null,
	reference varchar(20) not null,
	primary key (id,reference)
)
;
create table Account (
	id int not null,
	serial number(19) not null,
	name varchar(50) not null,
	primary key (id,serial)
)
;
create table User_Account (
	user_id int not null,
	user_reference varchar(20) not null,
	account_id int not null,
	account_serial number(19) not null,
	foreign key (user_id,user_reference) references UserX(id,reference) on delete cascade,
	foreign key (account_id,account_serial) references Account(id,serial)  on delete cascade
)
