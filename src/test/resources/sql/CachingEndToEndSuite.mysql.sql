[many-to-many]
create table Product (
	id int not null,
	name varchar(100) not null,
	primary key(id)
) engine InnoDB
;
create table Attribute (
	id int not null,
	name varchar(100) not null,
	value varchar(100) not null,
	primary key(id)
) engine InnoDB
;
create table Product_Attribute (
	product_id int not null,
	attribute_id int not null,
	primary key(product_id,attribute_id),
	foreign key (product_id) references Product(id) on update cascade on delete cascade,
	foreign key (attribute_id) references Attribute(id) on update cascade on delete cascade
) engine InnoDB

[one-to-many]
create table Person (
	id serial not null,
	name varchar(100) not null,
	primary key (id)
)  engine InnoDB
;
create table House (
	id serial not null,
	address varchar(100) not null,
	person_id bigint unsigned not null,
	primary key (id),
	constraint FK_House_Person foreign key (person_id) references Person(id)
		on update cascade on delete cascade 
)  engine InnoDB
