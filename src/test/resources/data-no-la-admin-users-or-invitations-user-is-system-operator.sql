INSERT INTO one_login_user (id, created_date)
VALUES ('urn:fdc:gov.uk:2022:UVWXY', '10/14/24'),
       ('urn:fdc:gov.uk:2022:A', '10/14/24');

INSERT INTO local_authority_user (subject_identifier, is_manager, local_authority_id, created_date, last_modified_date,
                                  name, email, has_accepted_privacy_notice)
VALUES ('urn:fdc:gov.uk:2022:A', false, 4, '10/14/24', '10/14/24', 'A name', 'test3@example.com', true);

INSERT INTO local_authority_invitation (invited_email, inviting_authority_id, token, invited_as_admin)
VALUES     ('H@example.com', 4, 'e98583f2-91b1-46ed-afb1-6cfcbaf97e65', false);


INSERT INTO system_operator (id, created_date, last_modified_date, subject_identifier)
VALUES (1,'2025-02-19 12:01:07.575927+00',null,'urn:fdc:gov.uk:2022:UVWXY');
