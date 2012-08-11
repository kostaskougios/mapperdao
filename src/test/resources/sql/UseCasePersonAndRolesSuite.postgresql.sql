[ddl]

CREATE TABLE person
(
  id character varying(40) NOT NULL,
  firstname character varying(255),
  lastname character varying(255),
  CONSTRAINT pk_party PRIMARY KEY (id )
)
;
CREATE TABLE roletype
(
  name character varying(40) NOT NULL,
  description character varying(255),
  CONSTRAINT pk_roleypte PRIMARY KEY (name )
)
;
CREATE TABLE singlepartyrole
(
  person_id character varying(40) NOT NULL,
  roletype_name character varying(40) NOT NULL,
  fromDate timestamp with time zone,
  toDate timestamp with time zone,
  CONSTRAINT pk_spr PRIMARY KEY (person_id , roletype_name ),
  CONSTRAINT fk_spr_person FOREIGN KEY (person_id)
      REFERENCES person (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT fk_to_role FOREIGN KEY (roletype_name)
      REFERENCES roletype (name) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
;
CREATE TABLE interpartyrelationship
(
  from_id character varying(40) NOT NULL,
  to_id character varying(40) NOT NULL,
  fromdate timestamp with time zone,
  todate timestamp with time zone,
  CONSTRAINT pk_ipr PRIMARY KEY (from_id , to_id ),
  CONSTRAINT fk_ipr_from FOREIGN KEY (from_id)
      REFERENCES person (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_ipr_to FOREIGN KEY (to_id)
      REFERENCES person (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
