[ddl]
create table JobPosition (
	id int not null,
	name varchar(100) not null,
	start timestamp,
	"end" timestamp,
	rank int not null,
	married boolean not null,
  version int not null,
	primary key (id)
)