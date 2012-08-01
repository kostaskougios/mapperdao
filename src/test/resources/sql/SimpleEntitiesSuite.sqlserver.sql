[ddl]
create table JobPosition (
	id int not null,
	name varchar(100) not null,
	start datetime,
	[end] datetime,
	rank int not null,
	married bit not null,
	primary key (id)
)