[ddl]
create table User (
	id serial not null,
	name varchar(50) not null,
	reference varchar(20) not null,
	primary key (id,reference)
) engine InnoDB
;
create table Account (
	id serial not null,
	serial bigint not null,
	name varchar(50) not null,
	primary key (id,serial)
) engine InnoDB
;
create table User_Account (
	user_id bigint unsigned not null,
	user_reference varchar(20) not null,
	account_id bigint unsigned not null,
	account_serial bigint not null,
	foreign key (user_id,user_reference) references User(id,reference) on delete cascade,
	foreign key (account_id,account_serial) references Account(id,serial)  on delete cascade
) engine InnoDB
