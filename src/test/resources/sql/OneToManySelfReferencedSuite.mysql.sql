[ddl]
create table Person (
	id serial not null,
	name varchar(100) not null,
	friend_id bigint unsigned,
	primary key (id),
	foreign key (friend_id) references Person(id) on delete cascade
)
