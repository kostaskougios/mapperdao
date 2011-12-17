[ddl]
create table test_insert (
	id int not null,
	name varchar(100) not null,
	dt datetime,
	primary key (id)
)
;
CREATE TABLE test_generatedkeys
(
	id int NOT NULL identity(1,1),
	name text,
	dt datetime,
	primary key (id)
)
