[without-sequences]
create table JobPosition (
	id int not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	name varchar(100) not null,
	primary key (id)
)