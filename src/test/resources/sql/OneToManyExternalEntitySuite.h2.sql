[ddl]
create table Person (
	id int not null identity(1,1),
	name varchar(100) not null,
	primary key (id)
)