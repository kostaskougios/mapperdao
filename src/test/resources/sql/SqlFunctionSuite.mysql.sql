[functions]
drop function if exists companyA 
;
drop function if exists addition
;
drop function if exists sub
;

CREATE FUNCTION companyA(cname varchar(50)) 
RETURNS boolean 
begin
	return cname = 'company A';
end
;
CREATE FUNCTION addition(v int, howMany int) 
RETURNS int
begin
	return v + howMany;
end
;
CREATE FUNCTION sub(v int, howMany int) 
RETURNS int
begin
	return v - howMany;
end
