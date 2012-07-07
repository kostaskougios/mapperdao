[ddl]
create table Person(
	id serial not null,
	name varchar(100) not null,
	age float not null,
	primary key(id)
)
;
create table Company(
	id serial not null,
	name varchar(100) not null,
	registration varchar(100) not null,
	primary key(id)
)
;

create table ContactList(
	id serial not null,
	name varchar(100) not null,
	primary key(id)
)
;

create table ContactList_Person(
	contactlist_id int not null,
	person_id int not null,
	primary key (contactlist_id,person_id),
	constraint FK_ContactList_Person_contactlist_id foreign key (contactlist_id) references ContactList(id) on delete cascade,
	constraint FK_person_id foreign key (person_id) references Person(id) on delete cascade,
)
;

create table ContactList_Company(
	contactlist_id int not null,
	company_id int not null,
	primary key (contactlist_id,company_id),
	constraint FK_ContactList_Company_contactlist_id foreign key (contactlist_id) references ContactList(id) on delete cascade,
	constraint FK_company_id foreign key (company_id) references Person(id) on delete cascade,
)
;
