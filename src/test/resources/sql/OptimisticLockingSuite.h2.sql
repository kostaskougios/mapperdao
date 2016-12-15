[ddl]
create table JobPosition (
	id int not null,
	name varchar(100) not null,
	start timestamp not null,
	end timestamp not null,
	rank int not null,
	married bit not null,
	version int not null,
	primary key (id)
)