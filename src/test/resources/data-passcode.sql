INSERT INTO one_login_user (id, created_date)
VALUES ('urn:fdc:gov.uk:2022:UVWXY', '10/14/24'),
('urn:fdc:gov.uk:2022:ABCDE', '10/14/24');

INSERT INTO passcode (passcode, local_authority_id, created_date, last_modified_date, subject_identifier)
VALUES ('FREE01', 2, current_date, null, null),
    ('TAKEN1', 2, current_date, null, 'urn:fdc:gov.uk:2022:ABCDE');

