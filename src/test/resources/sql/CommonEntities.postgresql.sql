[product-attribute]
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

[person-company]
create table Company(
	id serial not null,
	name varchar(40) not null,
	primary key (id)
)
;
create table Person(
	id serial not null,
	name varchar(40) not null,
	company_id int not null,
	primary key (id),
	foreign key (company_id) references Company(id) on delete cascade on update cascade
)

[owner-house]
create table Owner (
	id serial primary key,
	name varchar(20) not null
)
;
create table House (
	id serial primary key,
	address varchar(20) not null,
	owner_id int not null,
	foreign key (owner_id) references Owner(id) on delete cascade on update cascade
)
[husband-wife]
create table Wife(
	id serial not null,
	name varchar(20) not null,
	age int not null,
	primary key (id)
)
;
create table Husband(
	name varchar(20) not null,
	age int not null,
	wife_id int not null,
	constraint FK_Husband_Wife foreign key (wife_id) references Wife(id)
)

[image]
create table Image(
	id serial primary key,
	name varchar(20) not null,
	data bytea not null
)