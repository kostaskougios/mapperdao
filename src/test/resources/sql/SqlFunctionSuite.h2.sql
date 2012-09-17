[functions]
drop ALIAS if exists companyA 
;
drop ALIAS if exists addition
;
drop ALIAS if exists sub
;

CREATE ALIAS companyA AS '
boolean addition(String cname){
 return "company A".equals(cname);
}
';

CREATE ALIAS addition AS '
int addition(int v,int howMany){
 return v+howMany;
}
';

CREATE ALIAS sub AS '
int sub(int v,int howMany){
 return v-howMany;
}
';
