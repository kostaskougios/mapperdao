[ddl]
create table test_insert (
	id int not null,
	name varchar(100),
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
;
create table test_blob (
	id int NOT NULL identity(1,1) primary key,
	name varchar(100),
	data varBinary(MAX) not null
)
