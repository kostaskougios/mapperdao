[simple]
create table Cat (
	id int not null,
	name varchar(20) not null,
	parent_id int,
	primary key (id),
	foreign key (parent_id) references Cat(id)
)

[intid]
create table CatIId (
	id serial not null,
	name varchar(20) not null,
	parent_id int,
	primary key (id),
	foreign key (parent_id) references CatIId(id)
)

[longid]
create table CatLId (
	id serial not null,
	name varchar(20) not null,
	parent_id int,
	primary key (id),
	foreign key (parent_id) references CatLId(id)
)