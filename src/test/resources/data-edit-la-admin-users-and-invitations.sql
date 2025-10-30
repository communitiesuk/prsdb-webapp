INSERT INTO one_login_user (id, created_date)
VALUES ('urn:fdc:gov.uk:2022:UVWXY', '10/14/24'),
       ('urn:fdc:gov.uk:2022:A', '01/15/25'),
       ('urn:fdc:gov.uk:2022:B', '01/15/25');

INSERT INTO local_authority_user (id, subject_identifier, is_manager, local_authority_id, created_date, last_modified_date,
                                  name, email, has_accepted_privacy_notice)
VALUES (1, 'urn:fdc:gov.uk:2022:A', true, 1, '10/14/24', '10/14/24', 'Art Name', 'art@example.com', true),
       (2, 'urn:fdc:gov.uk:2022:B', true, 1, '10/09/24', '10/09/24', 'Bart name', 'bart@example.com', true);

INSERT INTO local_authority_invitation (id, invited_email, inviting_authority_id, token, invited_as_admin)
VALUES     (1, 'cart@example.com', 1, 'e98583f2-91b1-46ed-afb1-6cfcbaf97e65', true),
           (2, 'dart@example.com', 1, 'e98583f2-91b1-46ed-afb1-6cfcbaf97e67', true);


INSERT INTO system_operator (id, created_date, last_modified_date, subject_identifier)
VALUES (1,'2025-02-19 12:01:07.575927+00',null,'urn:fdc:gov.uk:2022:UVWXY');
