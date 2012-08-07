[bd]
create table BD (
	id int not null,
	big numeric(38,10),
	bool numeric(1),
	nv varchar(50),
	tx clob,
	primary key (id)
)

[obd]
create table OBD (
	id int not null,
	big numeric(38,10),
	bool smallint,
	nv varchar(50),
	bt number(2),
	small smallint,
	int int,
	"long" number(38),
	"float" float,
	"double" numeric(20,10),
	primary key (id)
)