[ddl]
CREATE TABLE person
(
  id varchar(40) NOT NULL,
  firstname varchar(255),
  lastname varchar(255),
  CONSTRAINT pk_party PRIMARY KEY (id )
)
;
CREATE TABLE roletype
(
  name varchar(40) NOT NULL,
  description varchar(255),
  CONSTRAINT pk_roleypte PRIMARY KEY (name )
)
;
CREATE TABLE singlepartyrole
(
  person_id varchar(40) NOT NULL,
  roletype_name varchar(40) NOT NULL,
  fromDate timestamp,
  toDate timestamp,
  CONSTRAINT pk_spr PRIMARY KEY (person_id , roletype_name),
  CONSTRAINT fk_spr_person FOREIGN KEY (person_id)
      REFERENCES person (id)
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT fk_to_role FOREIGN KEY (roletype_name)
      REFERENCES roletype (name)
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
;
CREATE TABLE interpartyrelationship
(
  from_id varchar(40) NOT NULL,
  to_id varchar(40) NOT NULL,
  fromdate timestamp,
  todate timestamp,
  CONSTRAINT pk_ipr PRIMARY KEY (from_id , to_id),
  CONSTRAINT fk_ipr_from FOREIGN KEY (from_id)
      REFERENCES person (id) 
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_ipr_to FOREIGN KEY (to_id)
      REFERENCES person (id)
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
