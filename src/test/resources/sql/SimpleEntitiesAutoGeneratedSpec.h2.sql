[with-sequences]
create table JobPosition (
	id int not null default nextval('myseq'),
	name varchar(100) not null,
	primary key (id)
)

[without-sequences]
create table JobPosition (
	id serial not null,
	name varchar(100) not null,
	primary key (id)
)
