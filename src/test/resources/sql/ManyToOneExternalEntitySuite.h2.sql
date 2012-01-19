[ddl]
create table Person (
	id serial not null,
	name varchar(100) not null,
	house_id int not null,
	primary key(id)
)