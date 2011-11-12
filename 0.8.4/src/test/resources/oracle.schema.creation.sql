-- please run as sys

create user testcaseuser identified by sigkill ;
 
grant resource to testcaseuser;

grant create table to testcaseuser;
grant create session to testcaseuser;
grant create sequence to testcaseuser;
grant create procedure to testcaseuser;
grant create trigger to testcaseuser;
grant create type to testcaseuser;
grant create view to testcaseuser;
grant create snapshot to testcaseuser;
grant create any materialized view to testcaseuser;
grant debug connect session to testcaseuser;

alter user testcaseuser quota unlimited on users;
