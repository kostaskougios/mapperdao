[ddl]
create table Category (
	id serial primary key,
	name varchar(100) not null,
	parent_id int,
	constraint FK_Category_Parent foreign key (parent_id) references Category(id) on delete cascade on update cascade
)