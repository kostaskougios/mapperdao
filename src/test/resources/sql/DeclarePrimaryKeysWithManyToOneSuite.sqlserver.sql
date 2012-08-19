[ddl]
create table Person (
	email varchar(50) not null,
	name varchar(20) not null,
	primary key (email)
)
;
create table LinkedPeople (
	from_id varchar(50) not null,
	to_id varchar(50) not null,
	note varchar(100),
	primary key (from_id,to_id),
	constraint FK_LinkedPeople_From foreign key (from_id) references Person(email) on delete cascade,
	constraint FK_LinkedPeople_To foreign key (to_id) references Person(email)
)
;
create trigger LinkedPeople_TO
on Person
instead of delete
as
begin
	DELETE	
	FROM Linkedpeople
	where to_id in (select email from deleted)

	DELETE	
	FROM Person
	where email in (select email from deleted)
end