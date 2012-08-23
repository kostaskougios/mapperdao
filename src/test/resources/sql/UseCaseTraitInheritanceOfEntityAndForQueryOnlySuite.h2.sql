[ddl]
create table Product(
	refCode varchar(20) not null,
	name varchar(40) not null,
	primary key (refCode)
)
;
create table Tag(
	tag varchar(40) not null,
	product_refCode varchar(20) not null,
	primary key (tag,product_refCode),
	constraint FK_Tag_Product foreign key (product_refCode) references Product(refCode)
)