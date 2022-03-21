--liquibase formatted sql
--changeset ek:v002 splitStatements=true endDelimiter:;

INSERT INTO brands (id, brandname, brandversion, creationdate) VALUES (1, 'gucci', '0.1', '2021-12-04 09:11:26');
INSERT INTO brands (id, brandname, brandversion, creationdate) VALUES (4, 'cc', '0.1', '2021-12-07 15:45:22');
INSERT INTO brands (id, brandname, brandversion, creationdate) VALUES (8, 'kiton', '0.1', '2021-12-13 15:02:31');