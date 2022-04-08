
delete from items;
delete from brands;

alter sequence hibernate_sequence restart with 10;

INSERT INTO brands (id, brandname, brandversion, creationdate) VALUES (1, 'gucci', '0.1', '2021-12-04 09:11:26');
INSERT INTO brands (id, brandname, brandversion, creationdate) VALUES (2, 'cc', '0.1', '2021-12-07 15:45:22');
INSERT INTO brands (id, brandname, brandversion, creationdate) VALUES (3, 'kiton', '0.1', '2021-12-13 15:02:31');

INSERT INTO items (id, serial, item_type, brand_id, parent_id, creationdate) VALUES (1, 100, 'PACK', 1, null, '2021-12-26 06:28:55');
INSERT INTO items (id, serial, item_type, brand_id, parent_id, creationdate) VALUES (2, 100, 'PACK', 2, null, '2021-12-26 17:53:50');
INSERT INTO items (id, serial, item_type, brand_id, parent_id, creationdate) VALUES (3, 100, 'ITEM', 1, 1, '2021-12-26 07:04:37');
INSERT INTO items (id, serial, item_type, brand_id, parent_id, creationdate) VALUES (4, 100, 'ITEM', 1, 1, '2021-12-26 17:57:34');
INSERT INTO items (id, serial, item_type, brand_id, parent_id, creationdate) VALUES (5, 100, 'ITEM', 2, 2, '2021-12-26 17:58:06');
INSERT INTO items (id, serial, item_type, brand_id, parent_id, creationdate) VALUES (6, 100, 'ITEM', 2, 2, '2021-12-26 18:00:15');
INSERT INTO items (id, serial, item_type, brand_id, parent_id, creationdate) VALUES (7, 100, 'ITEM', 2, 2, '2021-12-26 17:58:08');
INSERT INTO items (id, serial, item_type, brand_id, parent_id, creationdate) VALUES (8, 100, 'PACK', 3, null, '2021-12-26 17:58:08');
INSERT INTO items (id, serial, item_type, brand_id, parent_id, creationdate) VALUES (9, 100, 'ITEM', 3, 8, '2021-12-26 17:58:08');