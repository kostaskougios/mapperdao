[universe]
create table Universe (
	id serial primary key,
	name nvarchar(100) not null,
)
;
create table Galaxy (
	id serial primary key,
	name nvarchar(100) not null,
	universe_id int not null,
	constraint FK_Galaxy_Universe foreign key (universe_id) references Universe(id) on delete cascade on update cascade
)
;

create table Star (
	id serial primary key,
	name nvarchar(100) not null,
	type int not null
)
;

create table Galaxy_Star (
	galaxy_id int not null,
	star_id int not null,
	primary key (galaxy_id,star_id),
	constraint FK_Galaxy_Star_Galaxy foreign key (galaxy_id) references Galaxy(id) on delete cascade on update cascade,
	constraint FK_Galaxy_Star_Star foreign key (star_id) references Star(id) on delete cascade on update cascade
)
;

create table Star_Universe (
	star_id int not null,
	universe_id int not null,
	primary key (star_id,universe_id),
	constraint FK_Star_Universe_Star foreign key (star_id) references Star(id) on delete cascade on update cascade,
	constraint FK_Star_Universe_Universe foreign key (universe_id) references Universe(id) on delete cascade on update cascade
)
;

[product-attribute]
create table Product (
	id serial not null,
	name nvarchar(100) not null,
	primary key (id)
)
;
create table Attribute (
	id serial not null,
	name nvarchar(100) not null,
	value nvarchar(100) not null,
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
	id serial not null,
	name varchar(20) not null,
	primary key (id)
)
;
create table House (
	id serial not null,
	address varchar(20) not null,
	owner_id int not null,
	primary key (id),
	foreign key (owner_id) references Owner(id)  on delete cascade on update cascade
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
	constraint FK_Husband_Wife foreign key (wife_id) references Wife(id) on delete cascade
)

[image]
create table Image(
	id serial primary key,
	name varchar(20) not null,
	data blob not null
)