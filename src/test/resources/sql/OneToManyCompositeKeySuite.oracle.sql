[ddl]
create table House (
	id int not null,
	address varchar(50) not null,
	primary key (id,address)
)
;
create table Door (
	house_id int not null,
	house_address varchar(50) not null,
	location varchar(20) not null,
	constraint FK_Door_House foreign key(house_id,house_address) references House(id,address)
		on delete cascade
)