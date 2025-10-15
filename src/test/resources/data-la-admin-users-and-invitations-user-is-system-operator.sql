INSERT INTO one_login_user (id, created_date)
VALUES ('urn:fdc:gov.uk:2022:UVWXY', '10/14/24'),
       ('urn:fdc:gov.uk:2022:A', '01/15/25'),
       ('urn:fdc:gov.uk:2022:B', '01/15/25'),
       ('urn:fdc:gov.uk:2022:C', '01/15/25'),
       ('urn:fdc:gov.uk:2022:D', '01/15/25'),
       ('urn:fdc:gov.uk:2022:E', '01/15/25');

INSERT INTO local_authority_user (subject_identifier, is_manager, local_authority_id, created_date, last_modified_date,
                                  name, email, has_accepted_privacy_notice)
VALUES ('urn:fdc:gov.uk:2022:D', true, 5, '10/14/24', '10/14/24', 'D name', 'test3@example.com', true),
       ('urn:fdc:gov.uk:2022:E', true, 2, '10/09/24', '10/09/24', 'E name', 'Arthur.Dent4@test.com', true),
       ('urn:fdc:gov.uk:2022:A', true, 5, '10/14/24', '10/14/24', 'A name', 'test2@example.com', true),
       ('urn:fdc:gov.uk:2022:B', true, 5, '10/09/24', '10/09/24', 'B name', 'Arthur.Dent3@test.com', true),
       ('urn:fdc:gov.uk:2022:C', true, 2, '10/07/24', '10/07/24', 'C name', 'Ford.Prefect2@test.com', true);

INSERT INTO local_authority_invitation (invited_email, inviting_authority_id, token, invited_as_admin)
VALUES     ('H@example.com', 5, 'e98583f2-91b1-46ed-afb1-6cfcbaf97e65', true),
           ('I@example.com', 5, 'e98583f2-91b1-46ed-afb1-6cfcbaf97e67', true),
           ('F@example.com', 5, 'e98583f2-91b1-46ed-afb1-6cfcbaf97e63', true),
           ('G@example.com', 2, 'e98583f2-91b1-46ed-afb1-6cfcbaf97e64', true),
           ('J@example.com', 2, 'e98583f2-91b1-46ed-afb1-6cfcbaf97e66', true),
           ('K@example.com', 4, 'e98583f2-91b1-46ed-afb1-6cfcbaf97e68', true),
           ('L@example.com', 4, 'e98583f2-91b1-46ed-afb1-6cfcbaf97e69', true),
           ('M@example.com', 4, 'e98583f2-91b1-46ed-afb1-6cfcbaf97e70', true),
           ('N@example.com', 4, 'e98583f2-91b1-46ed-afb1-6cfcbaf97e71', true);


INSERT INTO system_operator (id, created_date, last_modified_date, subject_identifier)
VALUES (1,'2025-02-19 12:01:07.575927+00',null,'urn:fdc:gov.uk:2022:UVWXY');
