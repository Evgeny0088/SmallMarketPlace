--liquibase formatted sql
--changeset ek:remove-childItems-from-items-table splitStatements=true endDelimiter:;

alter table items drop column childcount;