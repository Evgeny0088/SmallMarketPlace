--liquibase formatted sql
--changeset ek:create-itemsbefore-test splitStatements=true endDelimiter:;

SET FOREIGN_KEY_CHECKS=0;

delete from brands;
delete from items;

insert into brands(id, brandname, brandversion) values (1, 'gucci', '0.1');
insert into brands(id, brandname, brandversion) values (2, 'prada', '0.1');
insert into brands(id, brandname, brandversion) values (3, 'cc', '0.1');

insert into items(id,serial,item_type,brand_id,parent_id) values (1,'1','PACK',1,null);
insert into items(id,serial,item_type,brand_id,parent_id) values (2,'1','ITEM',1,1);
insert into items(id,serial,item_type,brand_id,parent_id) values (3,'1','ITEM',1,1);