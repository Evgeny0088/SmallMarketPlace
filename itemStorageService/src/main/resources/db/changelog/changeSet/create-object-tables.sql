--liquibase formatted sql
--changeset ek:create-object-tables splitStatements=true endDelimiter:;

create table brands (
    id bigint not null auto_increment,
    brandname varchar(255) not null,
    brandversion varchar(255) not null,
    creationdate TIMESTAMP,
    constraint unique_bname unique (brandname),
    primary key (id)
);
--rollback DROP TABLE
--rollback brands
create table items(
    id bigint not null auto_increment,
    serial bigint not null,
    item_type varchar(255) not null,
    brand_id bigint not null,
    parent_id bigint,
    childcount bigint,
    creationdate TimeStamp,
    constraint brandId_id_fk foreign key (brand_id) references brands (id),
    primary key (id)
);
--rollback DROP TABLE
--rollback items
create table brandspage(
    id bigint not null auto_increment,
    openpagedate TimeStamp,
    primary key (id)
);
--rollback DROP TABLE
--rollback brandspage