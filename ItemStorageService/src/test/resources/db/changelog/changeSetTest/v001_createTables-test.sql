--liquibase formatted sql
--changeset ek:v001 splitStatements=true endDelimiter:;

create table brands (
    id bigserial not null primary key,
    brandname varchar(255) not null,
    brandversion varchar(255) not null,
    creationdate TIMESTAMP,
    constraint unique_bname unique (brandname)
);
--rollback DROP TABLE
--rollback brands
create table items(
    id bigserial not null primary key,
    serial bigint not null,
    item_type varchar(255) not null,
    brand_id bigint not null,
    parent_id bigint,
    creationdate TimeStamp,
    constraint brandId_id_fk foreign key (brand_id) references brands (id),
    constraint parent_id_fk foreign key (parent_id) references items (id)
);

--rollback DROP TABLE
--rollback items