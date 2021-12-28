--liquibase formatted sql
--changeset ek:add-constraint-to-parentItem splitStatements=true endDelimiter:;

alter table items add constraint parent_id_fk foreign key (parent_id) references items (id);
