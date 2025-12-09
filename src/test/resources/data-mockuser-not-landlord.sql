INSERT INTO one_login_user (id, created_date)
VALUES ('urn:fdc:gov.uk:2022:UVWXY', '10/14/24');

INSERT INTO address (id, created_date, last_modified_date, uprn, single_line_address, local_council_id, postcode, building_number)
VALUES  (1, '05/02/25', '05/02/25', 1013, '1 PRSDB Square, EG1 2AA', 2, 'EG1 2AA', '1'),
        (2, '05/02/25', '05/02/25', 1014, '2 PRSDB Square, EG1 2AA', 2, 'EG1 2AA', '2'),
        (3, '05/02/25', '05/02/25', 1015, '3 PRSDB Square, EG1 2AA', 2, 'EG1 2AA', '3'),
        (4, '05/02/25', '05/02/25', 1016, '4 PRSDB Square, EG1 2AA', 2, 'EG1 2AA', '4'),
        (5, '05/02/25', '05/02/25', 1017, '5 PRSDB Square, EG1 2AA', 2, 'EG1 2AA', '5');

SELECT setval(pg_get_serial_sequence('address', 'id'), (SELECT MAX(id) FROM address));
