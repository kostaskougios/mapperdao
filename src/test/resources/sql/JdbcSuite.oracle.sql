[ddl]
create table test_insert (
	id integer not null,
	name varchar(100),
	dt timestamp,
	primary key (id)
)
;
CREATE TABLE test_generatedkeys
(
	id number NOT NULL,
	name varchar(100),
	dt timestamp,
	primary key (id)
)
;
create or replace trigger tg_autonumber
before insert on test_generatedkeys for each row
begin
	select myseq.nextval into :new.id from dual;
end;
