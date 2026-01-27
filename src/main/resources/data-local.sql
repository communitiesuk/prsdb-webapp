INSERT INTO one_login_user (id, created_date)
VALUES ('urn:fdc:gov.uk:2022:ABCDE', '09/13/24'),
       ('urn:fdc:gov.uk:2022:FGHIJ', '09/13/24'),
       ('urn:fdc:gov.uk:2022:KLMNO', '10/07/24'),
       ('urn:fdc:gov.uk:2022:UVWXY', '10/14/24'),
       ('urn:fdc:gov.uk:2022:PQRST', '10/09/24'),
       ('urn:fdc:gov.uk:2022:07lXHJeQwE0k5PZO7w_PQF425vT8T7e63MrvyPYNSoI', '10/07/24'),
       ('urn:fdc:gov.uk:2022:mwfvbb5GgiDh0acjz9EDDQ7zwskWZzUSnWfavL70f6s', '10/02/24'),
       ('urn:fdc:gov.uk:2022:mGHDySEVfCsvfvc6lVWf6Qt9Dv0ZxPQWKoEzcjnBlUo', '10/15/24'),
       ('urn:fdc:gov.uk:2022:n93slCXHsxJ9rU6-AFM0jFIctYQjYf0KN9YVuJT-cao', '10/15/24'),
       ('urn:fdc:gov.uk:2022:cgVX2oJWKHMwzm8Gzx25CSoVXixVS0rw32Sar4Om8vQ', '10/15/24'),
       ('urn:fdc:gov.uk:2022:A', '01/15/25'),
       ('urn:fdc:gov.uk:2022:B', '01/15/25'),
       ('urn:fdc:gov.uk:2022:C', '01/15/25'),
       ('urn:fdc:gov.uk:2022:D', '01/15/25'),
       ('urn:fdc:gov.uk:2022:E', '01/15/25'),
       ('urn:fdc:gov.uk:2022:F', '01/15/25'),
       ('urn:fdc:gov.uk:2022:G', '01/15/25'),
       ('urn:fdc:gov.uk:2022:H', '01/15/25'),
       ('urn:fdc:gov.uk:2022:I', '01/15/25'),
       ('urn:fdc:gov.uk:2022:J', '01/15/25'),
       ('urn:fdc:gov.uk:2022:K', '01/15/25'),
       ('urn:fdc:gov.uk:2022:L', '01/15/25'),
       ('urn:fdc:gov.uk:2022:M', '01/15/25'),
       ('urn:fdc:gov.uk:2022:N', '01/15/25'),
       ('urn:fdc:gov.uk:2022:O', '01/15/25'),
       ('urn:fdc:gov.uk:2022:P', '01/15/25'),
       ('urn:fdc:gov.uk:2022:Q', '01/15/25'),
       ('urn:fdc:gov.uk:2022:R', '01/15/25'),
       ('urn:fdc:gov.uk:2022:S', '01/15/25'),
       ('urn:fdc:gov.uk:2022:T', '01/15/25'),
       ('urn:fdc:gov.uk:2022:U', '01/15/25'),
       ('urn:fdc:gov.uk:2022:V', '01/15/25'),
       ('urn:fdc:gov.uk:2022:W', '01/15/25'),
       ('urn:fdc:gov.uk:2022:X', '01/15/25'),
       ('urn:fdc:gov.uk:2022:Y', '01/15/25'),
       ('urn:fdc:gov.uk:2022:Z', '01/15/25'),
       ('urn:fdc:gov.uk:2022:GzFopg--2AyE6XtssVWwQTPELVQFupHJOjpONWS2uz0', '05/01/25');

INSERT INTO form_context (id, created_date, last_modified_date, journey_type, context, subject_identifier)
VALUES (1, current_date, current_date, 3, '{"lookup-address":{"houseNameOrNumber":"1","postcode":"WC2R 1LA"},"looked-up-addresses":"[{\"singleLineAddress\":\"1, SAVOY COURT, LONDON, WC2R 0EX\",\"localCouncilId\":318,\"uprn\":100023432931,\"buildingNumber\":\"1\",\"streetName\":\"SAVOY COURT\",\"townName\":\"LONDON\",\"postcode\":\"WC2R 0EX\"}]","select-address":{"address":"1, SAVOY COURT, LONDON, WC2R 0EX"},"property-type":{"customPropertyType":"","propertyType":"DETACHED_HOUSE"}}','urn:fdc:gov.uk:2022:UVWXY'),
       (2, '01/15/25', '01/15/25', 7, '{}','urn:fdc:gov.uk:2022:UVWXY'),
       (3, '01/15/25', '01/15/25', 7, '{}','urn:fdc:gov.uk:2022:UVWXY'),
       (4, '01/15/25', '01/15/25', 7, '{}','urn:fdc:gov.uk:2022:UVWXY'),
       (5, '2025-01-15 00:00:00+00', null, 7, '{}','urn:fdc:gov.uk:2022:mGHDySEVfCsvfvc6lVWf6Qt9Dv0ZxPQWKoEzcjnBlUo'),
       (6, '2025-01-15 00:00:00+00', null, 7, '{}','urn:fdc:gov.uk:2022:mGHDySEVfCsvfvc6lVWf6Qt9Dv0ZxPQWKoEzcjnBlUo'),
       (7, '2025-01-15 00:00:00+00', null, 7, '{}','urn:fdc:gov.uk:2022:mGHDySEVfCsvfvc6lVWf6Qt9Dv0ZxPQWKoEzcjnBlUo');

SELECT setval(pg_get_serial_sequence('form_context', 'id'), (SELECT MAX(id) FROM form_context));

INSERT INTO local_council_user (subject_identifier, is_manager, local_council_id, created_date, last_modified_date,
                                  name, email, has_accepted_privacy_notice)
VALUES ('urn:fdc:gov.uk:2022:KLMNO', true, 1, '10/07/24', '10/07/24', 'Ford Prefect', 'Ford.Prefect@test.com', true),
       ('urn:fdc:gov.uk:2022:UVWXY', true, 1, '10/14/24', '10/14/24', 'Mock User', 'test@example.com', true),
       ('urn:fdc:gov.uk:2022:PQRST', false, 1, '10/09/24', '10/09/24', 'Arthur Dent', 'Arthur.Dent@test.com', true),
       ('urn:fdc:gov.uk:2022:07lXHJeQwE0k5PZO7w_PQF425vT8T7e63MrvyPYNSoI', true, 1, '10/09/24', '10/09/24',
        'Jasmin Conterio',
        'jasmin.conterio@softwire.com', true),
       ('urn:fdc:gov.uk:2022:mwfvbb5GgiDh0acjz9EDDQ7zwskWZzUSnWfavL70f6s', true, 1, '10/02/24', '10/02/24',
        'Isobel Ibironke',
        'isobel.ibironke@softwire.com', true),
       ('urn:fdc:gov.uk:2022:n93slCXHsxJ9rU6-AFM0jFIctYQjYf0KN9YVuJT-cao', true, 1, '10/15/24', '10/15/24',
        'PRSDB LA Admin',
        'Team-PRSDB+laadmin@softwire.com', true),
       ('urn:fdc:gov.uk:2022:cgVX2oJWKHMwzm8Gzx25CSoVXixVS0rw32Sar4Om8vQ', false, 1, '10/15/24', '10/15/24',
        'PRSDB La User',
        'Team-PRSDB+lauser@softwire.com', true);

SELECT setval(pg_get_serial_sequence('local_council_user', 'id'), (SELECT MAX(id) FROM local_council_user));

INSERT INTO local_council_invitation (invited_email, inviting_council_id, token, invited_as_admin, created_date)
VALUES ('expired.invitation+a@example.com', 1, '1234abcd-5678-abcd-1234-567abcd1111a', false, '05/05/2025'),
       ('expired.invitation+b@example.com', 1, '1234abcd-5678-abcd-1234-567abcd1111b', false, '05/05/2025'),
       ('expired.invitation+c@example.com', 1, '1234abcd-5678-abcd-1234-567abcd1111c', false, '05/05/2025'),
       ('expired.invitation+d@example.com', 1, '1234abcd-5678-abcd-1234-567abcd1111d', false, '05/05/2025');

SELECT setval(pg_get_serial_sequence('local_council_invitation', 'id'), (SELECT MAX(id) FROM local_council_invitation));

INSERT INTO registration_number (id, created_date, number, type)
VALUES (1, '09/13/24', 2001001001, 1),
       (2, '09/13/24', 3002001002, 1),
       (3, '10/07/24', 4003001003, 1),
       (4, '10/14/24', 5004001004, 1),
       (5, '10/09/24', 6005001005, 1),
       (6, '12/10/24', 7006001006, 0),
       (7, '12/19/24', 8005001005, 1),
       (8, '01/15/25', 1001001001, 1),
       (9, '01/15/25', 1001001002, 1),
       (10, '01/15/25', 1001001003, 1),
       (11, '01/15/25', 1001001004, 1),
       (12, '01/15/25', 1001001005, 1),
       (13, '01/15/25', 1001001006, 1),
       (14, '01/15/25', 1001001007, 1),
       (15, '01/15/25', 1001001008, 1),
       (16, '01/15/25', 1001001009, 1),
       (17, '01/15/25', 1001001010, 1),
       (18, '01/15/25', 1001001011, 1),
       (19, '01/15/25', 1001001012, 1),
       (20, '01/15/25', 1001001013, 1),
       (21, '01/15/25', 1001001014, 1),
       (22, '01/15/25', 1001001015, 1),
       (23, '01/15/25', 1001001016, 1),
       (24, '01/15/25', 1001001017, 1),
       (25, '01/15/25', 1001001018, 1),
       (26, '01/15/25', 1001001019, 1),
       (27, '01/15/25', 1001001020, 1),
       (28, '01/15/25', 1001001021, 1),
       (29, '01/15/25', 1001001022, 1),
       (30, '01/15/25', 1001001023, 1),
       (31, '01/15/25', 1001001024, 1),
       (32, '01/15/25', 1001001025, 1),
       (33, '01/15/25', 100100106, 1),
       (34, '12/10/24', 0006001002, 0),
       (35, '12/10/24', 0006001003, 0),
       (36, '12/10/24', 0006001004, 0),
       (37, '12/10/24', 0006001005, 0),
       (38, '12/10/24', 0006001006, 0),
       (39, '02/02/25', 0006001007, 0),
       (40, '12/10/24', 0006001008, 0),
       (41, '12/10/24', 0006001009, 0),
       (42, '12/10/24', 0006001010, 0),
       (43, '12/10/24', 0006001011, 0),
       (44, '12/10/24', 0006001012, 0),
       (45, '12/10/24', 0006001013, 0),
       (46, '12/10/24', 0006001014, 0),
       (47, '12/10/24', 0006001015, 0),
       (48, '12/10/24', 0006001016, 0),
       (49, '12/10/24', 0006001017, 0),
       (50, '12/10/24', 0006001018, 0),
       (51, '12/10/24', 0006001019, 0),
       (52, '12/10/24', 0006001020, 0),
       (53, '12/10/24', 0006001021, 0),
       (54, '12/10/24', 0006001022, 0),
       (55, '12/10/24', 0006001023, 0),
       (56, '12/10/24', 0006001024, 0),
       (57, '12/10/24', 0006001025, 0),
       (58, '12/10/24', 0006001026, 0),
       (59, '12/10/24', 0006001027, 0),
       (60, '12/10/24', 0006001028, 0),
       (61, '12/10/24', 0006001029, 0),
       (62, '12/10/24', 0006001030, 0),
       (63, '12/10/24', 0006001031, 0),
       (64, '12/10/24', 0006001032, 0),
       (65, '2025-01-15 00:00:00+00', 83811499802, 0),
       (66, '2025-01-15 00:00:00+00', 40666195053, 0),
       (67, '2025-01-15 00:00:00+00', 150242309330, 0),
       (68, '2025-01-15', 1502423330, 0);

SELECT setval(pg_get_serial_sequence('registration_number', 'id'), (SELECT MAX(id) FROM registration_number));

INSERT INTO address (id, created_date, last_modified_date, uprn, single_line_address, local_council_id, postcode, building_number)
VALUES (1, '09/13/24', '09/13/24', 1, '1 Fictional Road, FA1 1AA', 1, 'FA1 1AA', '1'),
       (2, '09/13/24', '09/13/24', 2, '2 Fake Way', 1, 'FA1 1AB', '2'),
       (3, '09/13/24', '09/13/24', 3, '3 Imaginary Street', 1, 'FA1 1AC', '3'),
       (4, '09/13/24', '09/13/24', 4, '4 Pretend Crescent', 1, 'FA1 1AD', '4'),
       (5, '09/13/24', '09/13/24', 5, '5 Mythical Place', 1, 'FA1 1AE', '5'),
       (6, '12/10/2024', '12/10/2024', 1123456, '1, Example Road, EG1 1AA', 1, 'EG1 1AA', '1'),
       (7, '09/13/24', '09/13/24', 6, '2 Fictional Road', 1, 'FA1 1AF', '2'),
       (8, '09/13/24', '09/13/24', 7, '3 Fake Way', 2, 'FA1 1AG', '3'),
       (9, '09/13/24', '09/13/24', 100090154790, '4 Imaginary Street', 1, 'FA1 1AH', '4'),
       (10, '09/13/24', '09/13/24', 9, '5 Pretend Crescent', 1, 'FA1 1AJ', '5'),
       (11, '09/13/24', '09/13/24', 10, '6 Mythical Place', 1, 'FA1 1AK', '6'),
       (12, '09/13/24', '09/13/24', null, '6 Mythical Place', 1, 'FA1 1AL', '6'),
       (13, '05/02/25', '05/02/25', 1013, '1 PRSDB Square, EG1 2AA', 1, 'EG1 2AA', '1'),
       (14, '05/02/25', '05/02/25', 1014, '2 PRSDB Square, EG1 2AA', 1, 'EG1 2AA', '2'),
       (15, '05/02/25', '05/02/25', 1015, '3 PRSDB Square, EG1 2AA', 1, 'EG1 2AA', '3'),
       (16, '05/02/25', '05/02/25', 1016, '4 PRSDB Square, EG1 2AA', 1, 'EG1 2AA', '4'),
       (17, '05/02/25', '05/02/25', 1017, '5 PRSDB Square, EG1 2AA', 1, 'EG1 2AA', '5'),
       (18, '05/02/25', '05/02/25', 1018, '1 PRSDB Square, EG1 2AF', 1, 'EG1 2AF', '1'),
       (19, '05/02/25', '05/02/25', 1019, '1 PRSDB Square, EG1 2AG', 1, 'EG1 2AG', '1'),
       (20, '05/02/25', '05/02/25', 1020, '1 PRSDB Square, EG1 2AH', 1, 'EG1 2AH', '1'),
       (21, '05/02/25', '05/02/25', 1021, '9 PRSDB Square, EG1 2AI', 1, 'EG1 2AI', '9'),
       (22, '05/02/25', '05/02/25', 1022, '10 PRSDB Square, EG1 2AJ', 1, 'EG1 2AJ', '10'),
       (23, '05/02/25', '05/02/25', 1023, '11 PRSDB Square, EG1 2AK', 1, 'EG1 2AK', '11'),
       (24, '05/02/25', '05/02/25', 1024, '12 PRSDB Square, EG1 2AL', 1, 'EG1 2AL', '12'),
       (25, '05/02/25', '05/02/25', 1025, '13 PRSDB Square, EG1 2AM', 1, 'EG1 2AM', '13'),
       (26, '05/02/25', '05/02/25', 1026, '14 PRSDB Square, EG1 2AN', 1, 'EG1 2AN', '14'),
       (27, '05/02/25', '05/02/25', 1027, '15 PRSDB Square, EG1 2AO', 1, 'EG1 2AO', '15'),
       (28, '05/02/25', '05/02/25', 1028, '16 PRSDB Square, EG1 2AP', 1, 'EG1 2AP', '16'),
       (29, '05/02/25', '05/02/25', 1029, '17 PRSDB Square, EG1 2AQ', 1, 'EG1 2AQ', '17'),
       (30, '05/02/25', '05/02/25', 1030, '18 PRSDB Square, EG1 2AR', 1, 'EG1 2AR', '18'),
       (31, '05/02/25', '05/02/25', 1031, '19 PRSDB Square, EG1 2AS', 1, 'EG1 2AS', '19'),
       (32, '05/02/25', '05/02/25', 1032, '20 PRSDB Square, EG1 2AT', 1, 'EG1 2AT', '20'),
       (33, '05/02/25', '05/02/25', 1033, '21 PRSDB Square, EG1 2AU', 1, 'EG1 2AU', '21'),
       (34, '05/02/25', '05/02/25', 1034, '22 PRSDB Square, EG1 2AV', 1, 'EG1 2AV', '22'),
       (35, '05/02/25', '05/02/25', 1035, '23 PRSDB Square, EG1 2AW', 2, 'EG1 2AW', '23'),
       (36, '05/02/25', '05/02/25', 1036, '24 PRSDB Square, EG1 2AX', 2, 'EG1 2AX', '24'),
       (37, '05/02/25', '05/02/25', 1037, '25 PRSDB Square, EG1 2AY', 2, 'EG1 2AY', '25'),
       (38, '2025-01-15 00:00:00+00', null, 100090154792, '5, PROVIDENCE WAY, WATERBEACH, CAMBRIDGE, CB25 9QH', 20, 'CB25 9QH', '5'),
       (39, '2025-01-15 00:00:00+00', null, 100090154788, '1, PROVIDENCE WAY, WATERBEACH, CAMBRIDGE, CB25 9QH', 20, 'CB25 9QH', '1'),
       (40, '2025-01-15 00:00:00+00', null, null, '2, PROVIDENCE WAY, WATERBEACH, CAMBRIDGE, CB25 9QH', 20, 'CB25 9QH', '2');

INSERT INTO address (id, created_date, last_modified_date, uprn, single_line_address, local_council_id, postcode, building_name)
VALUES (41, '09/13/24', '09/13/24', 1038, 'Registered House, PRSDB Road, AA3 1AB ', 1, 'AA3 1AB ', 'Registered House'),
       (42, '09/13/24', '09/13/24', 1039, 'Stage House, PRSDB Road, AA3 1AB ', 1, 'AA3 1AB ', 'Stage House'),
       (43, '09/13/24', '09/13/24', 1040, 'Slate House, PRSDB Square, AA3 1AB ', 1, 'AA3 1AB ', 'Slate House'),
       (44, '09/13/24', '09/13/24', 1041, 'Grate House, PRSDB Road, AA3 1AB ', 1, 'AA3 1AB ', 'Grate House'),
       (45, '09/13/24', '09/13/24', 1042, 'Slate House, PRSDB Road, AA3 1AB ', 1, 'AA3 1AB ', 'Slate House');

SELECT setval(pg_get_serial_sequence('address', 'id'), (SELECT MAX(id) FROM address));

INSERT INTO landlord (id, created_date, last_modified_date, registration_number_id, address_id, date_of_birth,
                      is_active, phone_number, subject_identifier, name, email, country_of_residence, is_verified, has_accepted_privacy_notice)
VALUES (1, '09/13/24', '09/13/24', 1, 1, '09/13/2000', true, 07111111111, 'urn:fdc:gov.uk:2022:UVWXY',
        'Alexander Smith', 'alex.surname@example.com', 'England or Wales', true, true),
       (2, '09/13/24', '09/13/24', 2, 2, '08/13/2001', true, 07111111111, 'urn:fdc:gov.uk:2022:ABCDE',
        'Alexandra Davies', 'alexandra.q.davies@example.com', 'England or Wales', true, true),
       (3, '09/13/24', '09/13/24', 3, 3, '07/13/1997', true, 07111111111, 'urn:fdc:gov.uk:2022:PQRST',
        'Evan Alexandrescu', 'unrelatedemail@test.com', 'England or Wales', true, true),
       (4, '09/13/24', '09/13/24', 4, 4, '06/13/1989', true, 07111111111,
        'urn:fdc:gov.uk:2022:07lXHJeQwE0k5PZO7w_PQF425vT8T7e63MrvyPYNSoI', 'Tobias Evans', 'tobyevans@example.com', 'England or Wales', true, true),
       (5, '09/13/24', '09/13/24', 5, 5, '05/13/1950', true, 07111111111,
        'urn:fdc:gov.uk:2022:mwfvbb5GgiDh0acjz9EDDQ7zwskWZzUSnWfavL70f6s', 'Margaret Mary Smith',
        'mm.smith@example.com', 'England or Wales', true, true),
       (6, '12/19/24', '12/19/24', 7, 5, '06/13/1989', true, 07111111111,
        'urn:fdc:gov.uk:2022:mGHDySEVfCsvfvc6lVWf6Qt9Dv0ZxPQWKoEzcjnBlUo', 'PRSDB Landlord',
        'Team-PRSDB+landlord@softwire.com', 'England or Wales', true, true),
       (7, '01/15/25', '01/15/25', 6, 5, '06/13/1989', true, 0, 'urn:fdc:gov.uk:2022:FGHIJ', 'PRSDB',
        'test@example.com', 'England or Wales', true, true),
       (8, '01/15/25', '01/15/25', 8, 5, '06/13/1989', true, 0, 'urn:fdc:gov.uk:2022:A', 'PRSDB', 'test@example.com', 'England or Wales', true, true),
       (9, '01/15/25', '01/15/25', 9, 5, '06/13/1989', true, 0, 'urn:fdc:gov.uk:2022:B', 'PRSDB', 'test@example.com', 'England or Wales', true, true),
       (10, '01/15/25', '01/15/25', 10, 5, '06/13/1989', true, 0, 'urn:fdc:gov.uk:2022:C', 'PRSDB', 'test@example.com', 'England or Wales', true, true),
       (11, '01/15/25', '01/15/25', 11, 5, '06/13/1989', true, 0, 'urn:fdc:gov.uk:2022:D', 'PRSDB', 'test@example.com', 'England or Wales', true, true),
       (12, '01/15/25', '01/15/25', 12, 5, '06/13/1989', true, 0, 'urn:fdc:gov.uk:2022:E', 'PRSDB', 'test@example.com', 'England or Wales', true, true),
       (13, '01/15/25', '01/15/25', 13, 5, '06/13/1989', true, 0, 'urn:fdc:gov.uk:2022:F', 'PRSDB', 'test@example.com', 'England or Wales', true, true),
       (14, '01/15/25', '01/15/25', 14, 5, '06/13/1989', true, 0, 'urn:fdc:gov.uk:2022:G', 'PRSDB', 'test@example.com', 'England or Wales', true, true),
       (15, '01/15/25', '01/15/25', 15, 5, '06/13/1989', true, 0, 'urn:fdc:gov.uk:2022:H', 'PRSDB', 'test@example.com', 'England or Wales', true, true),
       (16, '01/15/25', '01/15/25', 16, 5, '06/13/1989', true, 0, 'urn:fdc:gov.uk:2022:I', 'PRSDB', 'test@example.com', 'England or Wales', true, true),
       (17, '01/15/25', '01/15/25', 17, 5, '06/13/1989', true, 0, 'urn:fdc:gov.uk:2022:J', 'PRSDB', 'test@example.com', 'England or Wales', true, true),
       (18, '01/15/25', '01/15/25', 18, 5, '06/13/1989', true, 0, 'urn:fdc:gov.uk:2022:K', 'PRSDB', 'test@example.com', 'England or Wales', true, true),
       (19, '01/15/25', '01/15/25', 19, 5, '06/13/1989', true, 0, 'urn:fdc:gov.uk:2022:L', 'PRSDB', 'test@example.com', 'England or Wales', true, true),
       (20, '01/15/25', '01/15/25', 20, 5, '06/13/1989', true, 0, 'urn:fdc:gov.uk:2022:M', 'PRSDB', 'test@example.com', 'England or Wales', true, true),
       (21, '01/15/25', '01/15/25', 21, 5, '06/13/1989', true, 0, 'urn:fdc:gov.uk:2022:N', 'PRSDB', 'test@example.com', 'England or Wales', true, true),
       (22, '01/15/25', '01/15/25', 22, 5, '06/13/1989', true, 0, 'urn:fdc:gov.uk:2022:O', 'PRSDB', 'test@example.com', 'England or Wales', true, true),
       (23, '01/15/25', '01/15/25', 23, 5, '06/13/1989', true, 0, 'urn:fdc:gov.uk:2022:P', 'PRSDB', 'test@example.com', 'England or Wales', true, true),
       (24, '01/15/25', '01/15/25', 24, 5, '06/13/1989', true, 0, 'urn:fdc:gov.uk:2022:Q', 'PRSDB', 'test@example.com', 'England or Wales', true, true),
       (25, '01/15/25', '01/15/25', 25, 5, '06/13/1989', true, 0, 'urn:fdc:gov.uk:2022:R', 'PRSDB', 'test@example.com', 'England or Wales', true, true),
       (26, '01/15/25', '01/15/25', 26, 5, '06/13/1989', true, 0, 'urn:fdc:gov.uk:2022:S', 'PRSDB', 'test@example.com', 'England or Wales', true, true),
       (27, '01/15/25', '01/15/25', 27, 5, '06/13/1989', true, 0, 'urn:fdc:gov.uk:2022:T', 'PRSDB', 'test@example.com', 'England or Wales', true, true),
       (28, '01/15/25', '01/15/25', 28, 5, '06/13/1989', true, 0, 'urn:fdc:gov.uk:2022:U', 'PRSDB', 'test@example.com', 'England or Wales', true, true),
       (29, '01/15/25', '01/15/25', 29, 5, '06/13/1989', true, 0, 'urn:fdc:gov.uk:2022:V', 'PRSDB', 'test@example.com', 'England or Wales', true, true),
       (30, '01/15/25', '01/15/25', 30, 5, '06/13/1989', true, 0, 'urn:fdc:gov.uk:2022:W', 'PRSDB', 'test@example.com', 'England or Wales', true, true),
       (31, '01/15/25', '01/15/25', 31, 5, '06/13/1989', true, 0, 'urn:fdc:gov.uk:2022:X', 'PRSDB', 'test@example.com', 'England or Wales', true, true),
       (32, '01/15/25', '01/15/25', 32, 5, '06/13/1989', true, 0, 'urn:fdc:gov.uk:2022:Y', 'PRSDB', 'test@example.com', 'England or Wales', true, true),
       (33, '01/15/25', '01/15/25', 33, 5, '06/13/1989', true, 0, 'urn:fdc:gov.uk:2022:Z', 'PRSDB', 'test@example.com', 'England or Wales', true, true);

SELECT setval(pg_get_serial_sequence('landlord', 'id'), (SELECT MAX(id) FROM landlord));

INSERT INTO license (id, license_type, license_number)
VALUES (1, 1, 'L12345678'),
       (2, 2, 'L12345678'),
       (3, 3, 'L12345678'),
       (4, 1, 'L12345678'),
       (5, 2, 'L12345678'),
       (6, 3, 'L12345678'),
       (7, 0, 'L12345678'),
       (8, 0, 'L12345678');

SELECT setval(pg_get_serial_sequence('license', 'id'), (SELECT MAX(id) FROM license));

INSERT INTO property_ownership (id, is_active, ownership_type, current_num_households, current_num_tenants, registration_number_id, primary_landlord_id, address_id, created_date, last_modified_date, license_id, incomplete_compliance_form_id, property_build_type)
VALUES (1, true, 1, 1, 2, 6, 1, 6, '01/15/25', '02/02/25', null, 2, 1),
       (2, false, 1, 1, 2, 34, 2, 7, '01/15/25', '01/15/25', null, null, 1),
       (3, true, 1, 1, 2, 35, 4, 8, '01/15/25', '01/15/25', null, null, 1),
       (4, true, 1, 1, 2, 36, 1, 9, '01/15/25', '01/15/25', null, 3, 1),
       (5, true, 1, 1, 2, 37, 1, 10, '01/15/25', '01/15/25', null, null, 1),
       (6, false, 1, 1, 2, 38, 1, 11, '01/15/25', '01/15/25', null, null, 1),
       (7, true, 1, 0, 0, 39, 1, 12, '02/02/25', '02/02/25', 1, 4, 1),
       (8, true, 1, 1, 1, 40, 1, 13, '05/02/25', '01/15/25', null, null, 1),
       (9, true, 1, 0, 0, 41, 1, 14, '05/02/25', '01/15/25', null, null, 1),
       (10, true, 1, 0, 0, 42, 1, 15, '05/02/25', '01/15/25', null, null, 1),
       (11, true, 1, 0, 0, 43, 1, 16, '05/02/25', '01/15/25', null, null, 1),
       (12, true, 1, 0, 0, 44, 1, 17, '05/02/25', '01/15/25', null, null, 1),
       (13, true, 1, 0, 0, 45, 1, 18, '05/02/25', '01/15/25', null, null, 1),
       (14, true, 1, 0, 0, 46, 1, 19, '05/02/25', '01/15/25', null, null, 1),
       (15, true, 1, 0, 0, 47, 1, 20, '05/02/25', '01/15/25', null, null, 1),
       (16, true, 1, 0, 0, 48, 1, 21, '05/02/25', '01/15/25', null, null, 1),
       (17, true, 1, 0, 0, 49, 1, 22, '05/02/25', '01/15/25', null, null, 1),
       (18, true, 1, 0, 0, 50, 1, 23, '05/02/25', '01/15/25', null, null, 1),
       (19, true, 1, 0, 0, 51, 1, 24, '05/02/25', '01/15/25', null, null, 1),
       (20, true, 1, 0, 0, 52, 1, 25, '05/02/25', '01/15/25', null, null, 1),
       (21, true, 1, 0, 0, 53, 1, 26, '05/02/25', '01/15/25', null, null, 1),
       (22, true, 1, 0, 0, 54, 1, 27, '05/02/25', '01/15/25', null, null, 1),
       (23, true, 1, 0, 0, 55, 1, 28, '05/02/25', '01/15/25', null, null, 1),
       (24, true, 1, 0, 0, 56, 1, 29, '05/02/25', '01/15/25', null, null, 1),
       (25, true, 1, 0, 0, 57, 1, 30, '05/02/25', '01/15/25', null, null, 1),
       (26, true, 1, 0, 0, 58, 1, 31, '05/02/25', '01/15/25', 2, null, 1),
       (27, true, 1, 0, 0, 59, 1, 32, '05/02/25', '01/15/25', 3, null, 1),
       (28, true, 1, 0, 0, 60, 1, 33, '05/02/25', '01/15/25', 4, null, 1),
       (29, true, 1, 0, 0, 61, 1, 34, '05/02/25', '01/15/25', 5, null, 1),
       (30, true, 1, 0, 0, 62, 1, 35, '05/02/25', '01/15/25', 6, null, 1),
       (31, true, 1, 0, 0, 63, 1, 36, '05/02/25', '01/15/25', 7, null, 1),
       (32, true, 1, 0, 0, 64, 1, 37, '05/02/25', '01/15/25', 8, null, 1),
       (33, true, 0, 0, 0, 65, 1, 38,'2025-01-15 00:00:00+00', null, null, 5, 1),
       (34, true, 0, 0, 0, 66, 1, 39,'2025-01-15 00:00:00+00', null, null, 6, 1),
       (35, true, 0, 0, 0, 67, 1, 40,'2025-01-15 00:00:00+00', null, null, 7, 1),
       (36, true, 0, 0, 0, 68, 1, 41,'2025-01-15', '01/15/25', null, null, 1);

SELECT setval(pg_get_serial_sequence('property_ownership', 'id'), (SELECT MAX(id) FROM property_ownership));

INSERT INTO system_operator (id, created_date, last_modified_date, subject_identifier)
VALUES (1,'2025-02-19 12:01:07.575927+00',null,'urn:fdc:gov.uk:2022:UVWXY'),
       (2,'2025-05-01 12:01:07.575927+00',null,'urn:fdc:gov.uk:2022:GzFopg--2AyE6XtssVWwQTPELVQFupHJOjpONWS2uz0');

SELECT setval(pg_get_serial_sequence('system_operator', 'id'), (SELECT MAX(id) FROM system_operator));

INSERT INTO property_compliance (id, property_ownership_id, created_date, last_modified_date,
                                 gas_safety_upload_id, gas_safety_cert_issue_date, gas_safety_cert_engineer_num, gas_safety_cert_exemption_reason, gas_safety_cert_exemption_other_reason,
                                 eicr_upload_id, eicr_issue_date, eicr_exemption_reason, eicr_exemption_other_reason,
                                 epc_url, epc_expiry_date, tenancy_started_before_epc_expiry, epc_energy_rating, epc_exemption_reason, epc_mees_exemption_reason,
                                 has_fire_safety_declaration, has_keep_property_safe_declaration, has_responsibility_to_tenants_declaration)
VALUES (1, 8, '01/01/25', '01/01/25',
        null, null, null, null, null,
        null, null, null, null,
        null, null, null, null, null, null,
        true, true, true),
       (2, 9, '01/01/25', '01/01/25',
        null, '1990-02-28', null, null, null,
        null, '1990-02-28', null, null,
        'https://find-energy-certificate-staging.digital.communities.gov.uk/energy-certificate/0000-0000-0000-0961-0832', '2021-03-16', false, 'c', null, null,
        true, true, true),
       (3, 10, '01/01/25', '01/01/25',
        null, null, null, 0, null,
        null, null, 0, null,
        'https://find-energy-certificate-staging.digital.communities.gov.uk/energy-certificate/0000-0000-0000-1050-2867', '2031-02-28', null, 'g', null, null,
        true, true, true),
       (4, 11, '01/01/25', '01/01/25',
        null, null, null, 0, null,
        null, null, 0, null,
        'https://find-energy-certificate-staging.digital.communities.gov.uk/energy-certificate/0000-0000-0000-1050-2867', '2031-02-28', null, 'g', null, 0,
        true, true, true),
        (5, 12, '01/01/25', null,
        null, null, null, 0, null,
        null, null, 0, null,
        null, null, null, null, 1, null,
        true, true, true),
       (6, 33, '01/01/25', null,
        null, null, null, 0, null,
        null, null, 0, null,
        null, null, null, null, 1, null,
        true, true, true);

SELECT setval(pg_get_serial_sequence('property_compliance', 'id'), (SELECT MAX(id) FROM property_compliance));


INSERT INTO file_upload (id, created_date, status, object_key, e_tag, version_id, extension)
VALUES (1, '09/13/24', 1, 'file-key-123', 'e-tag-123', 'version-id-123', 'pdf');
SELECT setval(pg_get_serial_sequence('file_upload', 'id'), (SELECT MAX(id) FROM file_upload));

INSERT INTO certificate_upload (id, created_date, file_upload_id, property_ownership_id, category)
VALUES (1, '09/13/24', 1, 1, 1);
SELECT setval(pg_get_serial_sequence('certificate_upload', 'id'), (SELECT MAX(id) FROM certificate_upload));

INSERT INTO reminder_email_sent (id,last_reminder_email_sent_date)
VALUES (1, current_date-1),
       (2, current_date-1);
SELECT setval(pg_get_serial_sequence('reminder_email_sent', 'id'), (SELECT MAX(id) FROM reminder_email_sent));

INSERT INTO saved_journey_state (id, created_date, last_modified_date, journey_id, serialized_state, subject_identifier,reminder_email_sent_id)
VALUES (1, current_date-22,current_date-22, 'example-incomplete-journey1', '{"journeyData":{"lookup-address":{"houseNameOrNumber":"1","postcode":"WC2R1LA"},"select-address":{"address":"1,SAVOYCOURT,LONDON,WC2R0EX"},"property-type":{"customPropertyType":"","propertyType":"DETACHED_HOUSE"}},"cachedAddresses":"[{\"singleLineAddress\":\"1,SAVOYCOURT,LONDON,WC2R0EX\",\"localCouncilId\":1,\"uprn\":1038,\"buildingNumber\":\"1\",\"streetName\":\"SAVOYCOURT\",\"townName\":\"LONDON\",\"postcode\":\"WC2R0EX\"}]","isAddressAlreadyRegistered":"false"}', 'urn:fdc:gov.uk:2022:UVWXY', 1),
       (2, current_date-22,current_date-29, 'example-incomplete-journey2', '{"journeyData":{"lookup-address":{"houseNameOrNumber":"1","postcode":"WC2R1LA"},"select-address":{"address":"1,SAVOYCOURT,LONDON,WC2R0EX"},"property-type":{"customPropertyType":"","propertyType":"DETACHED_HOUSE"}},"cachedAddresses":"[{\"singleLineAddress\":\"1,SAVOYCOURT,LONDON,WC2R0EX\",\"localCouncilId\":1,\"uprn\":1038,\"buildingNumber\":\"1\",\"streetName\":\"SAVOYCOURT\",\"townName\":\"LONDON\",\"postcode\":\"WC2R0EX\"}]","isAddressAlreadyRegistered":"false"}', 'urn:fdc:gov.uk:2022:UVWXY', null),
       (3, current_date-9,current_date-9, 'example-incomplete-journey3', '{"journeyData":{"lookup-address":{"houseNameOrNumber":"1","postcode":"WC2R1LA"},"select-address":{"address":"1,SAVOYCOURT,LONDON,WC2R0EX"},"property-type":{"customPropertyType":"","propertyType":"DETACHED_HOUSE"}},"cachedAddresses":"[{\"singleLineAddress\":\"1,SAVOYCOURT,LONDON,WC2R0EX\",\"localCouncilId\":1,\"uprn\":1038,\"buildingNumber\":\"1\",\"streetName\":\"SAVOYCOURT\",\"townName\":\"LONDON\",\"postcode\":\"WC2R0EX\"}]","isAddressAlreadyRegistered":"false"}', 'urn:fdc:gov.uk:2022:UVWXY', null),
       (4, current_date-29,current_date-29, 'example-incomplete-journey4', '{"journeyData":{"lookup-address":{"houseNameOrNumber":"1","postcode":"WC2R1LA"},"select-address":{"address":"1,SAVOYCOURT,LONDON,WC2R0EX"},"property-type":{"customPropertyType":"","propertyType":"DETACHED_HOUSE"}},"cachedAddresses":"[{\"singleLineAddress\":\"1,SAVOYCOURT,LONDON,WC2R0EX\",\"localCouncilId\":1,\"uprn\":1038,\"buildingNumber\":\"1\",\"streetName\":\"SAVOYCOURT\",\"townName\":\"LONDON\",\"postcode\":\"WC2R0EX\"}]","isAddressAlreadyRegistered":"false"}', 'urn:fdc:gov.uk:2022:UVWXY', 2),
       (5, current_date-23,current_date-23, 'example-incomplete-journey5', '{"journeyData":{"lookup-address":{"houseNameOrNumber":"1","postcode":"WC2R1LA"},"select-address":{"address":"1,SAVOYCOURT,LONDON,WC2R0EX"},"property-type":{"customPropertyType":"","propertyType":"DETACHED_HOUSE"}},"cachedAddresses":"[{\"singleLineAddress\":\"1,SAVOYCOURT,LONDON,WC2R0EX\",\"localCouncilId\":1,\"uprn\":1038,\"buildingNumber\":\"1\",\"streetName\":\"SAVOYCOURT\",\"townName\":\"LONDON\",\"postcode\":\"WC2R0EX\"}]","isAddressAlreadyRegistered":"false"}', 'urn:fdc:gov.uk:2022:UVWXY', null);

SELECT setval(pg_get_serial_sequence('saved_journey_state', 'id'), (SELECT MAX(id) FROM saved_journey_state));

INSERT INTO landlord_incomplete_properties (landlord_id, saved_journey_state_id)
VALUES (1, 1),
       (1, 2),
       (1, 3),
       (1, 4),
       (1, 5);
