[ddl]
create table test_insert (
	id int not null,
	name varchar(100),
	dt timestamp,
	primary key (id)
)
;
CREATE TABLE test_generatedkeys (
	id int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	name varchar(100),
	dt timestamp,
	primary key (id)
)
