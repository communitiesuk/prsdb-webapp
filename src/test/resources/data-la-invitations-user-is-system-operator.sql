INSERT INTO one_login_user (id, created_date)
VALUES ('urn:fdc:gov.uk:2022:UVWXY', '10/14/24'),
       ('urn:fdc:gov.uk:2022:PQRST', '10/14/24'),
       ('urn:fdc:gov.uk:2022:ABCDE', '10/14/24');

INSERT INTO local_authority_user (subject_identifier, is_manager, local_authority_id, created_date, last_modified_date,
                                  name, email)
VALUES ('urn:fdc:gov.uk:2022:ABCDE', true, 2, '10/14/24', '10/14/24', 'Test Admin', 'test@example.com'),
    ('urn:fdc:gov.uk:2022:PQRST', false, 2, '10/09/24', '10/09/24', 'Arthur Dent', 'Arthur.Dent@test.com');

INSERT INTO local_authority_invitation (invited_email, inviting_authority_id, token, invited_as_admin)
VALUES ('invited.user@example.com', 2, 'e98583f2-91b1-46ed-afb1-6cfcbaf97e63', false),
       ('z.user@example.com', 2, 'e98583f2-91b1-46ed-afb1-6cfcbaf97e64', false),
       ('y.adminuser@example.com', 2, 'e98583f2-91b1-46ed-afb1-6cfcbaf97e65', true),
       ('x.adminuser@example.com', 2, 'e98583f2-91b1-46ed-afb1-6cfcbaf97e66', true);


INSERT INTO system_operator (id, created_date, last_modified_date, subject_identifier)
VALUES (1,'2025-02-19 12:01:07.575927+00',null,'urn:fdc:gov.uk:2022:UVWXY');