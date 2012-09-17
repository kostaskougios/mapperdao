[functions]

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[companyA]') AND type in (N'FN', N'IF', N'TF', N'FS', N'FT'))
DROP FUNCTION companyA
;

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[addition]') AND type in (N'FN', N'IF', N'TF', N'FS', N'FT'))
DROP FUNCTION addition
;

IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[sub]') AND type in (N'FN', N'IF', N'TF', N'FS', N'FT'))
DROP FUNCTION sub
;

CREATE FUNCTION companyA(@cname varchar(50)) RETURNS bit AS
begin
	if ( @cname = 'company A' )
	begin
		return 1
	end
	return 0
end
;

CREATE FUNCTION addition(@v int, @howMany int) RETURNS int AS
begin
	return @v + @howMany
end
;

CREATE FUNCTION sub(@v int, @howMany int) RETURNS int AS
begin
	return @v - @howMany;
end
