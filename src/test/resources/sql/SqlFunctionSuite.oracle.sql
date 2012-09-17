[functions]
CREATE or replace FUNCTION companyA(cname varchar(50)) 
RETURN boolean AS
begin
	return cname = 'company A';
end;
;
CREATE or replace FUNCTION addition(v int, howMany int) 
RETURN int AS
begin
	return v + howMany;
end;
;
CREATE or replace FUNCTION sub(v int, howMany int) 
RETURN int AS
begin
	return v - howMany;
end;
