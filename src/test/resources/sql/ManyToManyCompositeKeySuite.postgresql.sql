[ddl]
create table "User" (
	id serial not null,
	name varchar(50) not null,
	reference varchar(20) not null,
	primary key (id,reference)
)