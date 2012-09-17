[image]
create table Image(
	id serial primary key,
	name varchar(20) not null,
	data blob not null
)