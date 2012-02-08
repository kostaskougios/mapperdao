[ddl]
create table Directory (
	id serial not null,
	uri varchar(150) not null,
	parent_id int, -- a directory but can be null => no parent
	primary key (id),
	foreign key (parent_id) references Directory(id) on delete cascade on update cascade
)
;
create table File (
	id serial not null,
	uri varchar(150) not null,
	parent_id int not null, -- a directory
	fileType varchar(10) not null,
	primary key (id),
	foreign key (parent_id) references Directory(id) on delete cascade on update cascade
)
;
create table Archive (
	id serial not null,
	uri varchar(150) not null,
	parent_id int not null, -- a directory
	zipType varchar(10) not null,
	primary key (id),
	foreign key (parent_id) references Directory(id) on delete cascade on update cascade
)
;
