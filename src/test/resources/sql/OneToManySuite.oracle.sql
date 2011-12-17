[ddl]
create table Person (
	id int not null,
	name varchar(100) not null,
	surname varchar(100) not null,
	age int not null,
	primary key (id)
)
;
create table JobPosition (
	id int not null,
	name varchar(100) not null,
	"start" date,
	end date,
	rank int not null,
	person_id int not null,
	primary key (id),
	constraint FK_JobPosition_Person foreign key (person_id) references Person(id) on delete cascade
)
;
create table House (
	id int not null,
	address varchar(100) not null,
	person_id int not null,
	primary key (id),
	constraint FK_House_Person foreign key (person_id) references Person(id) on delete cascade
)
;
create or replace trigger cascade_update
after update of id on Person
for each row
begin
	update JobPosition
	set person_id = :new.id
	where person_id = :old.id;
	update House
	set person_id = :new.id
	where person_id = :old.id;
end;
