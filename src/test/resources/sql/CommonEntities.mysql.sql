[product-attribute]
create table Product (
	id serial not null,
	name nvarchar(100) not null,
	primary key (id)
) engine InnoDB
;
create table Attribute (
	id serial not null,
	name nvarchar(100) not null,
	value nvarchar(100) not null,
	primary key(id)
) engine InnoDB
;

create table Product_Attribute (
	product_id bigint unsigned not null,
	attribute_id bigint unsigned not null,
	primary key (product_id,attribute_id),
	foreign key (product_id) references Product(id) on delete cascade on update cascade,
	foreign key (attribute_id) references Attribute(id) on delete cascade on update cascade
) engine InnoDB

[person-company]
create table Company(
	id serial not null,
	name varchar(40) not null,
	primary key (id)
) engine InnoDB
;
create table Person(
	id serial not null,
	name varchar(40) not null,
	company_id bigint unsigned not null,
	primary key (id),
	foreign key (company_id) references Company(id) on delete cascade on update cascade
) engine InnoDB

[owner-house]
create table Owner (
	id serial not null,
	name varchar(20) not null,
	primary key (id)
) engine InnoDB
;
create table House (
	id serial not null,
	address varchar(20) not null,
	owner_id bigint unsigned  not null,
	primary key (id),
	foreign key (owner_id) references Owner(id)  on delete cascade on update cascade
) engine InnoDB

[husband-wife]
create table Wife(
	id serial not null,
	name varchar(20) not null,
	age int not null,
	primary key (id)
) engine InnoDB
;
create table Husband(
	name varchar(20) not null,
	age int not null,
	wife_id bigint unsigned not null,
	constraint FK_Husband_Wife foreign key (wife_id) references Wife(id) on delete cascade
) engine InnoDB

[image]
create table Image(
	id serial primary key,
	name varchar(20) not null,
	data blob not null
)