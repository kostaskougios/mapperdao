[bd]
create table BD (
	id int not null,
	big numeric(40,10),
	bool boolean,
	nv varchar(50),
	tx text,
	primary key (id)
)

[obd]
create table OBD (
	id int not null,
	big numeric(40,10),
	bool boolean,
	nv varchar(50),
	bt smallint,
	small smallint,
	int int,
	long bigint,
	float float,
	double numeric(20,10),
	primary key (id)
)

[dates]
create table Dates (
	id int not null,
	localDate timestamp,
	time time,
	primary key (id)
)