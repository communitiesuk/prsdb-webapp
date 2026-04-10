INSERT INTO prsdb_user (id, created_date)
VALUES ('urn:fdc:gov.uk:2022:UVWXY', '10/14/24'),
       ('urn:fdc:gov.uk:2022:ABCDE', '10/14/24');

INSERT INTO passcode (passcode, created_date, last_modified_date, subject_identifier)
VALUES ('FREE01', current_date, null, null),
       ('TAKEN1', current_date, null, 'urn:fdc:gov.uk:2022:ABCDE');
