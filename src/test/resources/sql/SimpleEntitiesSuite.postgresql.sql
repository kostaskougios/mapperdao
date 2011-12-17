[ddl]
create table JobPosition (
	id int not null,
	name varchar(100) not null,
	start timestamp with time zone,
	"end" timestamp with time zone,
	rank int not null,
	primary key (id)
)