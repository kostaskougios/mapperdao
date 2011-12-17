[ddl]
create table User (
	name varchar(20) not null,
	surname varchar(20) not null,
	age int not null,
	primary key (name,surname)
)
