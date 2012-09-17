[functions]
CREATE or replace FUNCTION companyA(IN cname varchar(50)) RETURNS boolean AS
$$
begin
	return cname = 'company A';
end
$$

LANGUAGE plpgsql VOLATILE;
;
CREATE or replace FUNCTION addition(IN v int, IN howMany int) RETURNS int AS
$$
begin
	return v + howMany;
end
$$

LANGUAGE plpgsql VOLATILE;
;
CREATE or replace FUNCTION sub(IN v int, IN howMany int) RETURNS int AS
$$
begin
	return v - howMany;
end
$$

LANGUAGE plpgsql VOLATILE;
