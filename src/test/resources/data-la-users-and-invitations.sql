INSERT INTO one_login_user (id, name, email, created_date, last_modified_date)
VALUES ('urn:fdc:gov.uk:2022:UVWXY', 'Mock User', 'test@example.com', '10/14/24', '10/14/24');

INSERT INTO local_authority (id, name, custodian_code, created_date, last_modified_date)
VALUES (1, 'Betelgeuse', '100', '09/13/24', '09/13/24');

INSERT INTO local_authority_user (subject_identifier, is_manager, local_authority_id, created_date, last_modified_date,
                                  name, email)
VALUES ('urn:fdc:gov.uk:2022:UVWXY', true, 1, '10/14/24', '10/14/24', 'Mock User', 'test@la.com');

INSERT INTO local_authority_invitation (invited_email, inviting_authority_id, token)
VALUES ('invited.user@example.com', 1, 'e98583f2-91b1-46ed-afb1-6cfcbaf97e63'),
       ('z.user@example.com', 1, 'e98583f2-91b1-46ed-afb1-6cfcbaf97e64');