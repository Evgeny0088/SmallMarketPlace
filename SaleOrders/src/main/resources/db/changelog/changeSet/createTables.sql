--liquibase formatted sql
--changeset ek:createTables splitStatements=true endDelimiter:;

create table main_page_statistic(
    id bigserial not null primary key,
    openpagedate TimeStamp
);
--rollback DROP TABLE
--rollback brands

