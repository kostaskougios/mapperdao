[ddl]
create table test_insert (
	id int not null,
	name varchar(100),
	dt datetime,
	primary key (id)
)
;
CREATE TABLE test_generatedkeys (
	id int primary key auto_increment,
	name varchar(100),
	dt datetime
)
;
create table test_blob (
	id serial primary key,
	name varchar(100),
	data blob not null
)
