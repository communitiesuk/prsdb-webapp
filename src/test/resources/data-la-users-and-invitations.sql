INSERT INTO one_login_user (id, created_date)
VALUES ('urn:fdc:gov.uk:2022:UVWXY', '10/14/24'),
       ('urn:fdc:gov.uk:2022:PQRST', '10/14/24');

INSERT INTO local_authority_user (subject_identifier, is_manager, local_authority_id, created_date, last_modified_date,
                                  name, email)
VALUES ('urn:fdc:gov.uk:2022:UVWXY', true, 2, '10/14/24', '10/14/24', 'Mock User', 'test@example.com'),
       ('urn:fdc:gov.uk:2022:PQRST', false, 2, '10/09/24', '10/09/24', 'Arthur Dent', 'Arthur.Dent@test.com');

INSERT INTO local_authority_invitation (invited_email, inviting_authority_id, token, invited_as_admin)
VALUES ('invited.user@example.com', 2, 'e98583f2-91b1-46ed-afb1-6cfcbaf97e63', false),
       ('z.user@example.com', 2, 'e98583f2-91b1-46ed-afb1-6cfcbaf97e64', false),
       ('y.adminuser@example.com', 2, 'e98583f2-91b1-46ed-afb1-6cfcbaf97e65', true),
       ('x.adminuser@example.com', 2, 'e98583f2-91b1-46ed-afb1-6cfcbaf97e66', true);