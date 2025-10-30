INSERT INTO one_login_user (id, created_date)
VALUES ('urn:fdc:gov.uk:2022:UVWXY', '10/14/24'),
       ('urn:fdc:gov.uk:2022:A', '01/15/25'),
       ('urn:fdc:gov.uk:2022:B', '01/15/25'),
       ('urn:fdc:gov.uk:2022:C', '01/15/25'),
       ('urn:fdc:gov.uk:2022:D', '01/15/25'),
       ('urn:fdc:gov.uk:2022:E', '01/15/25');

INSERT INTO local_authority_user (id, subject_identifier, is_manager, local_authority_id, created_date, last_modified_date,
                                  name, email, has_accepted_privacy_notice)
VALUES (1, 'urn:fdc:gov.uk:2022:D', true, 4, '10/14/24', '10/14/24', 'D name', 'test3@example.com', true),
       (2, 'urn:fdc:gov.uk:2022:E', true, 1, '10/09/24', '10/09/24', 'E name', 'Arthur.Dent4@test.com', true),
       (3, 'urn:fdc:gov.uk:2022:A', true, 4, '10/14/24', '10/14/24', 'A name', 'test2@example.com', true),
       (4, 'urn:fdc:gov.uk:2022:B', true, 4, '10/09/24', '10/09/24', 'B name', 'Arthur.Dent3@test.com', true),
       (5, 'urn:fdc:gov.uk:2022:C', true, 1, '10/07/24', '10/07/24', 'C name', 'Ford.Prefect2@test.com', true);

INSERT INTO local_authority_invitation (id, invited_email, inviting_authority_id, token, invited_as_admin)
VALUES     (1, 'H@example.com', 4, 'e98583f2-91b1-46ed-afb1-6cfcbaf97e65', true),
           (2, 'I@example.com', 4, 'e98583f2-91b1-46ed-afb1-6cfcbaf97e67', true),
           (3, 'F@example.com', 4, 'e98583f2-91b1-46ed-afb1-6cfcbaf97e63', true),
           (4, 'G@example.com', 1, 'e98583f2-91b1-46ed-afb1-6cfcbaf97e64', true),
           (5, 'J@example.com', 1, 'e98583f2-91b1-46ed-afb1-6cfcbaf97e66', true),
           (6, 'K@example.com', 3, 'e98583f2-91b1-46ed-afb1-6cfcbaf97e68', true),
           (7, 'L@example.com', 3, 'e98583f2-91b1-46ed-afb1-6cfcbaf97e69', true),
           (8, 'M@example.com', 3, 'e98583f2-91b1-46ed-afb1-6cfcbaf97e70', true),
           (9, 'N@example.com', 3, 'e98583f2-91b1-46ed-afb1-6cfcbaf97e71', true);


INSERT INTO system_operator (id, created_date, last_modified_date, subject_identifier)
VALUES (1,'2025-02-19 12:01:07.575927+00',null,'urn:fdc:gov.uk:2022:UVWXY');
