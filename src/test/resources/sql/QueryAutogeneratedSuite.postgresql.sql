[ddl]
create table company (
  id serial not null,
  name varchar(255) not null,
  constraint pk_company primary key (id))
;

create table computer (
  id serial not null,
  name varchar(255) not null,
  introduced timestamp,
  discontinued timestamp,
  company_id int,
  constraint pk_computer primary key (id))
;

alter table computer add constraint fk_computer_company
foreign key (company_id) references company (id) 
on delete cascade on update cascade
;
