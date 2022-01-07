create table brands
(
    id bigint auto_increment primary key,
    brandname varchar(255) not null,
    brandversion varchar(255) not null,
    creationdate timestamp,
    constraint unique_bname unique (brandname)
);

INSERT INTO SmallMarketPlace.brands (id, brandname, brandversion, creationdate) VALUES (1, 'gucci', '0.1', '2021-12-04 09:11:26');
INSERT INTO SmallMarketPlace.brands (id, brandname, brandversion, creationdate) VALUES (4, 'cc', '0.1', '2021-12-07 15:45:22');
INSERT INTO SmallMarketPlace.brands (id, brandname, brandversion, creationdate) VALUES (8, 'kiton', '0.1', '2021-12-13 15:02:31');
