--liquibase formatted sql
--changeset ek:createTables splitStatements=true endDelimiter:;

create sequence hibernate_sequence start with 1 increment by 1;

create table main_page_statistic(
    id bigserial not null primary key,
    openpagedate TimeStamp
);
--rollback DROP TABLE
--rollback brands
