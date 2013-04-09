[ddl]
create table Product (
	id serial not null,
	name varchar(100) not null,
	primary key (id)
)
;
create table Attribute (
	id serial not null,
	name varchar(100) not null,
	value varchar(100) not null,
	primary key(id)
)
;

create table Product_Attribute (
	product_id int not null,
	attribute_id int not null,
	primary key (product_id,attribute_id),
	foreign key (product_id) references Product(id) on delete cascade on update cascade,
	foreign key (attribute_id) references Attribute(id) on delete cascade on update cascade
)
;
create table Log (
	id serial not null,
	change varchar(100) not null,
	primary key(id)
)
;
create table Product_Log (
	product_id int not null,
	log_id int not null,
	primary key (product_id,log_id),
	foreign key (product_id) references Product(id) on delete cascade on update cascade,
	foreign key (log_id) references Log(id) on delete cascade on update cascade
)
;
create table Attribute_Log (
	attribute_id int not null,
	log_id int not null,
	primary key (attribute_id,log_id),
	foreign key (attribute_id) references Attribute(id) on delete cascade on update cascade,
	foreign key (log_id) references Log(id) on delete cascade on update cascade
)
