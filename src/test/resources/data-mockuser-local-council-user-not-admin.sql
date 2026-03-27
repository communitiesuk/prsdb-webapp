INSERT INTO prsdb_user (id, created_date)
VALUES ('urn:fdc:gov.uk:2022:UVWXY', '10/14/24'),
       ('ia-mock-user-12345', '10/14/24');

INSERT INTO local_council_user (subject_identifier, is_manager, local_council_id, created_date, last_modified_date,
                                  name, email, has_accepted_privacy_notice)
VALUES ('ia-mock-user-12345', false, 2, '10/14/24', '10/14/24', 'Mock User', 'test@example.com', true);
