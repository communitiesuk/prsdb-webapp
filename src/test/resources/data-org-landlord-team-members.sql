INSERT INTO prsdb_user (id, created_date)
VALUES ('urn:fdc:gov.uk:2022:ORG02', '07/23/26'),
       ('urn:fdc:gov.uk:2022:ORG03', '07/23/26');

INSERT INTO organisational_landlord_user (organisation_landlord_id, subject_identifier, name, email, role, created_date)
VALUES (36, 'urn:fdc:gov.uk:2022:ORG02', 'Beth Admin', 'beth.admin@example.com', 0, '07/23/26'),
       (36, 'urn:fdc:gov.uk:2022:ORG03', 'Carl Editor', 'carl.editor@example.com', 1, '07/23/26');
