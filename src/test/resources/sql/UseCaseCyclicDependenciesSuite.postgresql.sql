[ddl]

CREATE TABLE BusDriver
(
  id serial NOT NULL,
  name character varying(255) NOT NULL,
  PRIMARY KEY (id)
)
;
CREATE TABLE People
(
  id serial NOT NULL,
  name character varying(80),
  PRIMARY KEY (id)
)
;
CREATE TABLE Commute
(
  id character varying(40) NOT NULL,
  name character varying(80) NOT NULL,
  people_id integer NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_commute_people FOREIGN KEY (people_id) REFERENCES People(id)
)
;
-- Table: testmapperdao2.visit

-- DROP TABLE testmapperdao2.visit;

CREATE TABLE Visit
(
  id character varying(40) NOT NULL,
  people_id integer NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_visit_people FOREIGN KEY (people_id)
      REFERENCES people(id)
)
;

create table People_BusDriver (
	people_id int not null,
	busdriver_id int not null,
	constraint FK_PBD_People foreign key (people_id) references People(id) on delete cascade on update cascade,
	constraint FK_PBD_BusDriver foreign key (busdriver_id) references BusDriver(id) on delete cascade on update cascade
)
;

create table Commute_Visit (
	commute_id character varying(40) not null,
	visit_id character varying(40) not null,
	constraint FK_CV_Commute foreign key (commute_id) references Commute(id) on delete cascade on update cascade,
	constraint FK_CV_Visit foreign key (visit_id) references Visit(id) on delete cascade on update cascade
)