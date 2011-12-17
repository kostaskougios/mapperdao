[ddl]
create table Person (
	id serial not null,
	name varchar(100) not null,
	friend_id int,
	primary key (id),
	foreign key (friend_id) references Person(id) on delete cascade
)
