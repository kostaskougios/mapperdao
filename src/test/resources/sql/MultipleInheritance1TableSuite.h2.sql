[ddl]
create table Reminder (
        id serial,
        type smallint not null,
        hourOfDay smallint not null,
        dayOfWeek smallint not null,
        time timestamp,
        primary key (id)
)
