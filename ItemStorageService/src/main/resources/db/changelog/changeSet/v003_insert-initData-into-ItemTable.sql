--liquibase formatted sql
--changeset ek:v003 splitStatements=true endDelimiter:;

INSERT INTO items (id, serial, item_type, brand_id, parent_id, creationdate) VALUES (72, 100, 'PACK', 4, null, '2021-12-26 06:28:55');
INSERT INTO items (id, serial, item_type, brand_id, parent_id, creationdate) VALUES (44, 100, 'ITEM', 4, 72, '2021-12-26 07:04:37');
INSERT INTO items (id, serial, item_type, brand_id, parent_id, creationdate) VALUES (75, 100, 'PACK', 1, null, '2021-12-26 17:53:50');
INSERT INTO items (id, serial, item_type, brand_id, parent_id, creationdate) VALUES (76, 100, 'PACK', 1, 75, '2021-12-26 17:57:34');
INSERT INTO items (id, serial, item_type, brand_id, parent_id, creationdate) VALUES (77, 100, 'ITEM', 1, 76, '2021-12-26 17:58:06');
INSERT INTO items (id, serial, item_type, brand_id, parent_id, creationdate) VALUES (78, 100, 'PACK', 4, null, '2021-12-26 18:00:15');
INSERT INTO items (id, serial, item_type, brand_id, parent_id, creationdate) VALUES (79, 100, 'ITEM', 1, 76, '2021-12-26 17:58:08');
