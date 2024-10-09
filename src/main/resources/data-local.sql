INSERT INTO one_login_user (id, name, email, created_date, last_modified_date)
VALUES ('urn:fdc:gov.uk:2022:ABCDE', 'Bob T Builder', 'bobthebuilder@gmail.com', '09/13/24', '09/13/24'),
       ('urn:fdc:gov.uk:2022:FGHIJ', 'Anne Other', 'Anne.Other@hotmail.com', '09/13/24', '09/13/24'),
       ('urn:fdc:gov.uk:2022:KLMNO', 'Ford Prefect', 'Ford.Prefect@hotmail.com', '10/07/24', '10/07/24');

INSERT INTO landlord_user (subject_identifier, phone_number, date_of_birth, created_date, last_modified_date)
VALUES ('urn:fdc:gov.uk:2022:ABCDE', '07712345678', '01/01/00', '09/13/24', '09/13/24'),
       ('urn:fdc:gov.uk:2022:FGHIJ', '07811111111', '11/23/98', '09/13/24', '09/13/24');

INSERT INTO local_authority (name, created_date, last_modified_date)
VALUES ('Betelgeuse','09/13/24', '09/13/24');

INSERT INTO local_authority_user (subject_identifier, is_manager, local_authority_id, created_date, last_modified_date)
VALUES ('urn:fdc:gov.uk:2022:KLMNO',true, 1,'10/07/24', '10/07/24')