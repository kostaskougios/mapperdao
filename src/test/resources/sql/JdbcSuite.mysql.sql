[ddl]
create table test_insert (
	id int not null,
	name varchar(100),
	dt datetime,
	primary key (id)
)
;
CREATE TABLE test_generatedkeys (
	id serial NOT NULL,
	name varchar(100),
	dt datetime,
	primary key (id)
)
