INSERT INTO license (id, license_type, license_number) VALUES (100, 0, 'L00000100');
SELECT setval(pg_get_serial_sequence('license', 'id'), (SELECT MAX(id) FROM license));
UPDATE property_ownership SET license_id = 100, license_provide_later = NULL WHERE id = 1;
