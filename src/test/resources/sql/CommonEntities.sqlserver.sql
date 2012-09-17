[image]
create table Image(
	id int identity(1,1) primary key,
	name varchar(20) not null,
	data varbinary(max) not null
)