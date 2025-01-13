INSERT INTO one_login_user (id, name, email, created_date, last_modified_date)
VALUES ('urn:fdc:gov.uk:2022:ABCDE', 'Bob T Builder', 'bobthebuilder@gmail.com', '09/13/24', '09/13/24'),
       ('urn:fdc:gov.uk:2022:FGHIJ', 'Anne Other', 'Anne.Other@hotmail.com', '09/13/24', '09/13/24'),
       ('urn:fdc:gov.uk:2022:KLMNO', 'Ford Prefect', 'Ford.Prefect@hotmail.com', '10/07/24', '10/07/24'),
       ('urn:fdc:gov.uk:2022:UVWXY', 'Mock User', 'test@example.com', '10/14/24', '10/14/24'),
       ('urn:fdc:gov.uk:2022:PQRST', 'Arthur Dent', 'Arthur.Dent@hotmail.com', '10/09/24', '10/09/24'),
       ('urn:fdc:gov.uk:2022:07lXHJeQwE0k5PZO7w_PQF425vT8T7e63MrvyPYNSoI', 'Jasmin Conterio',
        'jasmin.conterio@softwire.com', '10/07/24', '10/07/24'),
       ('urn:fdc:gov.uk:2022:mwfvbb5GgiDh0acjz9EDDQ7zwskWZzUSnWfavL70f6s', 'Isobel Ibironke',
        'isobel.ibironke@softwire.com', '10/02/24', '10/02/24'),
       ('urn:fdc:gov.uk:2022:mGHDySEVfCsvfvc6lVWf6Qt9Dv0ZxPQWKoEzcjnBlUo', 'PRSDB Landlord',
        'Team-PRSDB+landlord@softwire.com', '10/15/24', '10/15/24'),
       ('urn:fdc:gov.uk:2022:n93slCXHsxJ9rU6-AFM0jFIctYQjYf0KN9YVuJT-cao', 'PRSDB LA Admin',
        'Team-PRSDB+laadmin@softwire.com', '10/15/24', '10/15/24'),
       ('urn:fdc:gov.uk:2022:cgVX2oJWKHMwzm8Gzx25CSoVXixVS0rw32Sar4Om8vQ', 'PRSDB La User',
        'Team-PRSDB+lauser@softwire.com', '10/15/24', '10/15/24');

INSERT INTO landlord_user (subject_identifier, phone_number, date_of_birth, created_date, last_modified_date)
VALUES ('urn:fdc:gov.uk:2022:ABCDE', '07712345678', '01/01/00', '09/13/24', '09/13/24'),
       ('urn:fdc:gov.uk:2022:FGHIJ', '07811111111', '11/23/98', '09/13/24', '09/13/24'),
       ('urn:fdc:gov.uk:2022:UVWXY', '01406946277', '06/16/84', '10/14/24', '10/14/24'),
       ('urn:fdc:gov.uk:2022:07lXHJeQwE0k5PZO7w_PQF425vT8T7e63MrvyPYNSoI', '01223456789', '02/01/00', '10/09/24',
        '10/09/24'),
       ('urn:fdc:gov.uk:2022:mwfvbb5GgiDh0acjz9EDDQ7zwskWZzUSnWfavL70f6s', '01223456789', '02/01/00', '10/02/24',
        '10/02/24'),
       ('urn:fdc:gov.uk:2022:mGHDySEVfCsvfvc6lVWf6Qt9Dv0ZxPQWKoEzcjnBlUo', '01223456789', '03/05/00', '10/15/24',
        '10/09/24');

INSERT INTO local_authority (id, name, created_date, last_modified_date)
VALUES (1, 'Betelgeuse', '09/13/24', '09/13/24');

SELECT setval(pg_get_serial_sequence('local_authority', 'id'), (SELECT MAX(id) FROM local_authority));

INSERT INTO local_authority_user (subject_identifier, is_manager, local_authority_id, created_date, last_modified_date,
                                  name, email)
VALUES ('urn:fdc:gov.uk:2022:KLMNO', true, 1, '10/07/24', '10/07/24', 'Ford Prefect', 'Ford.Prefect@la.com'),
       ('urn:fdc:gov.uk:2022:UVWXY', true, 1, '10/14/24', '10/14/24', 'Mock User', 'test@la.com'),
       ('urn:fdc:gov.uk:2022:PQRST', false, 1, '10/09/24', '10/09/24', 'Arthur Dent', 'Arthur.Dent@la.com'),
       ('urn:fdc:gov.uk:2022:07lXHJeQwE0k5PZO7w_PQF425vT8T7e63MrvyPYNSoI', true, 1, '10/09/24', '10/09/24',
        'Jasmin Conterio',
        'jasmin.conterio@softwire.com'),
       ('urn:fdc:gov.uk:2022:mwfvbb5GgiDh0acjz9EDDQ7zwskWZzUSnWfavL70f6s', true, 1, '10/02/24', '10/02/24',
        'Isobel Ibironke',
        'isobel.ibironke@softwire.com'),
       ('urn:fdc:gov.uk:2022:n93slCXHsxJ9rU6-AFM0jFIctYQjYf0KN9YVuJT-cao', true, 1, '10/15/24', '10/15/24',
        'PRSDB LA Admin',
        'Team-PRSDB+laadmin@softwire.com'),
       ('urn:fdc:gov.uk:2022:cgVX2oJWKHMwzm8Gzx25CSoVXixVS0rw32Sar4Om8vQ', false, 1, '10/15/24', '10/15/24',
        'PRSDB La User',
        'Team-PRSDB+lauser@softwire.com');

INSERT INTO local_authority_invitation (invited_email, inviting_authority_id, token)
VALUES ('invited.user@example.com', 1, gen_random_uuid()),
       ('user.invited@example.com', 1, gen_random_uuid()),
       ('further.user@example.com', 1, gen_random_uuid()),
       ('another.user@example.com', 1, gen_random_uuid());

INSERT INTO registration_number (id, created_date, number, type)
VALUES (1, '09/13/24', 2001001001, 1),
       (2, '09/13/24', 3002001002, 1),
       (3, '10/07/24', 4003001003, 1),
       (4, '10/14/24', 5004001004, 1),
       (5, '10/09/24', 6005001005, 1),
       (6, '12/10/24', 7006001006, 0),
       (7, '12/19/24', 8005001005, 1);

SELECT setval(pg_get_serial_sequence('registration_number', 'id'), (SELECT MAX(id) FROM registration_number));

INSERT INTO address (id, created_date, last_modified_date, uprn, single_line_address)
VALUES (1, '09/13/24', '09/13/24', 1, '1 Fictional Road'),
       (2, '09/13/24', '09/13/24', 2, '2 Fake Way'),
       (3, '09/13/24', '09/13/24', 3, '3 Imaginary Street'),
       (4, '09/13/24', '09/13/24', 4, '4 Pretend Crescent'),
       (5, '09/13/24', '09/13/24', 5, '5 Mythical Place'),
       (6, '12/10/2024', '12/10/2024', 1123456, '1, Example Road, EG');

SELECT setval(pg_get_serial_sequence('address', 'id'), (SELECT MAX(id) FROM address));

INSERT INTO landlord (id, created_date, last_modified_date, registration_number_id, address_id, date_of_birth,
                      is_active, phone_number, subject_identifier, name, email)
VALUES (1, '09/13/24', '09/13/24', 1, 1, '09/13/2000', true, 07111111111, 'urn:fdc:gov.uk:2022:UVWXY',
        'Alexander Smith', 'alex.surname@example.com'),
       (2, '09/13/24', '09/13/24', 2, 2, '08/13/2001', true, 07111111111, 'urn:fdc:gov.uk:2022:ABCDE',
        'Alexandra Davies', 'alexandra.q.davies@example.com'),
       (3, '09/13/24', '09/13/24', 3, 3, '07/13/1997', true, 07111111111, 'urn:fdc:gov.uk:2022:PQRST',
        'Evan Alexandrescu', 'unrelatedemail@completelydifferentdomain.com'),
       (4, '09/13/24', '09/13/24', 4, 4, '06/13/1989', true, 07111111111,
        'urn:fdc:gov.uk:2022:07lXHJeQwE0k5PZO7w_PQF425vT8T7e63MrvyPYNSoI', 'Tobias Evans', 'tobyevans@importantco.com'),
       (5, '09/13/24', '09/13/24', 5, 5, '05/13/1950', true, 07111111111,
        'urn:fdc:gov.uk:2022:mwfvbb5GgiDh0acjz9EDDQ7zwskWZzUSnWfavL70f6s', 'Margaret Mary Smith',
        'mm.smith@importantco.com'),
        (6, '12/19/24', '12/19/24', 7,5,'06/13/1989',true,07111111111,
         'urn:fdc:gov.uk:2022:mGHDySEVfCsvfvc6lVWf6Qt9Dv0ZxPQWKoEzcjnBlUo','PRSDB Landlord', 'Team-PRSDB+landlord@softwire.com');

SELECT setval(pg_get_serial_sequence('landlord', 'id'), (SELECT MAX(id) FROM landlord));

INSERT INTO property (id, status, is_active, property_build_type, address_id)
VALUES (1, 1, true, 1, 6);

SELECT setval(pg_get_serial_sequence('property', 'id'), (SELECT MAX(id) FROM property));

INSERT INTO property_ownership (id, is_active, occupancy_type, landlord_type, ownership_type, current_num_households,
                                current_num_tenants,
                                registration_number_id, primary_landlord_id, property_id)
VALUES (1, true, 0, 0, 1, 1, 2, 6, 1, 1);

SELECT setval(pg_get_serial_sequence('property_ownership', 'id'), (SELECT MAX(id) FROM property_ownership));