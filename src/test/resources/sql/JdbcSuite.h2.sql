[ddl]
create table test_insert (
	id int not null,
	name varchar(100),
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
;
create table test_blob (
	id serial primary key,
	name varchar(100),
	data binary not null
)
