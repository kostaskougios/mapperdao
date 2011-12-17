[ddl]
create table JobPosition (
	id int not null,
	name varchar(100) not null,
	start timestamp with time zone,
	primary key (id)
)
