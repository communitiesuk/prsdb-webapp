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

INSERT INTO local_authority (name, created_date, last_modified_date)
VALUES ('Betelgeuse', '09/13/24', '09/13/24');

INSERT INTO local_authority_user (subject_identifier, is_manager, local_authority_id, created_date, last_modified_date)
VALUES ('urn:fdc:gov.uk:2022:KLMNO', true, 1, '10/07/24', '10/07/24'),
       ('urn:fdc:gov.uk:2022:UVWXY', true, 1, '10/14/24', '10/14/24'),
       ('urn:fdc:gov.uk:2022:PQRST', false, 1, '10/09/24', '10/09/24'),
       ('urn:fdc:gov.uk:2022:07lXHJeQwE0k5PZO7w_PQF425vT8T7e63MrvyPYNSoI', true, 1, '10/09/24', '10/09/24'),
       ('urn:fdc:gov.uk:2022:mwfvbb5GgiDh0acjz9EDDQ7zwskWZzUSnWfavL70f6s', true, 1, '10/02/24', '10/02/24'),
       ('urn:fdc:gov.uk:2022:n93slCXHsxJ9rU6-AFM0jFIctYQjYf0KN9YVuJT-cao', true, 1, '10/15/24', '10/15/24'),
       ('urn:fdc:gov.uk:2022:cgVX2oJWKHMwzm8Gzx25CSoVXixVS0rw32Sar4Om8vQ', false, 1, '10/15/24', '10/15/24');
