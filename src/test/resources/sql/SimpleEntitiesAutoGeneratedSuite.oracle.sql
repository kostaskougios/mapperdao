[with-sequences]
create table JobPosition (
	id int not null,
	name varchar(100) not null,
	primary key (id)
)

;

create or replace trigger ti_autonumber
before insert on JobPosition for each row
begin
	select myseq.nextval into :new.id from dual;
end;
