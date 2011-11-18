[ddl]
create table Person (
	id int not null identity(1,1),
	name varchar(100) not null,
	friend_id int,
	primary key (id),
	foreign key (friend_id) references Person(id)
)