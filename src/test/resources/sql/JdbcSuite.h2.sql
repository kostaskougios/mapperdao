[ddl]
create table test_insert (
	id int not null,
	name varchar(100) not null,
	dt timestamp,
	primary key (id)
)
;
CREATE TABLE test_generatedkeys
(
	id serial NOT NULL,
	name character varying,
	dt timestamp,
	primary key (id)
)
