[ddl]
create table "User" (
	id int not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	name varchar(50) not null,
	reference varchar(20) not null,
	primary key (id,reference)
)
;
create table Account (
	id int not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	serial bigint not null,
	name varchar(50) not null,
	primary key (id,serial)
)
;
create table User_Account (
	user_id int not null,
	user_reference varchar(20) not null,
	account_id int not null,
	account_serial bigint not null,
	foreign key (user_id,user_reference) references "User"(id,reference) on delete cascade,
	foreign key (account_id,account_serial) references Account(id,serial)  on delete cascade
)
