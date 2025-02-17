INSERT INTO one_login_user (id, name, email, created_date, last_modified_date)
VALUES ('urn:fdc:gov.uk:2022:mGHDySEVfCsvfvc6lVWf6Qt9Dv0ZxPQWKoEzcjnBlUo', 'PRSDB Landlord',
        'Team-PRSDB+landlord@softwire.com', '10/15/24', '10/15/24'),
       ('urn:fdc:gov.uk:2022:n93slCXHsxJ9rU6-AFM0jFIctYQjYf0KN9YVuJT-cao', 'PRSDB LA Admin',
        'Team-PRSDB+laadmin@softwire.com', '10/15/24', '10/15/24'),
       ('urn:fdc:gov.uk:2022:cgVX2oJWKHMwzm8Gzx25CSoVXixVS0rw32Sar4Om8vQ', 'PRSDB La User',
        'Team-PRSDB+lauser@softwire.com', '10/15/24', '10/15/24');

INSERT INTO landlord_user (subject_identifier, phone_number, date_of_birth, created_date, last_modified_date)
VALUES ('urn:fdc:gov.uk:2022:mGHDySEVfCsvfvc6lVWf6Qt9Dv0ZxPQWKoEzcjnBlUo', '01223456789', '03/05/00', '10/15/24',
        '10/09/24');

SELECT setval(pg_get_serial_sequence('local_authority', 'id'), (SELECT MAX(id) FROM local_authority));

INSERT INTO local_authority_user (subject_identifier, is_manager, local_authority_id, created_date, last_modified_date,
                                  name, email)
VALUES ('urn:fdc:gov.uk:2022:n93slCXHsxJ9rU6-AFM0jFIctYQjYf0KN9YVuJT-cao', true, 1, '10/15/24', '10/15/24',
        'PRSDB LA Admin',
        'Team-PRSDB+laadmin@softwire.com'),
       ('urn:fdc:gov.uk:2022:cgVX2oJWKHMwzm8Gzx25CSoVXixVS0rw32Sar4Om8vQ', false, 1, '10/15/24', '10/15/24',
        'PRSDB La User',
        'Team-PRSDB+lauser@softwire.com');

INSERT INTO registration_number (id, created_date, number, type)
VALUES (1, '10/15/24', 2001001001, 1),
       (2, '10/15/24', 3002001002, 1);

SELECT setval(pg_get_serial_sequence('registration_number', 'id'), (SELECT MAX(id) FROM registration_number));

INSERT INTO address (id, created_date, last_modified_date, uprn, single_line_address, local_authority_id)
VALUES (1, '10/15/24', '10/15/24', 1, '1 Fictional Road', 1),
       (2, '09/13/24', '09/13/24', 2, '2 Fake Way', 1);

SELECT setval(pg_get_serial_sequence('address', 'id'), (SELECT MAX(id) FROM address));

INSERT INTO landlord (id, created_date, last_modified_date, registration_number_id, address_id, date_of_birth,
                      is_active, phone_number, subject_identifier, name, email, country_of_residence)
VALUES (1, '10/15/24', '10/15/24', 1, 1, '05/13/1950', true, 07111111111,
        'urn:fdc:gov.uk:2022:mGHDySEVfCsvfvc6lVWf6Qt9Dv0ZxPQWKoEzcjnBlUo', 'PRSDB Landlord',
        'Team-PRSDB+landlord@softwire.com', 'England or Wales');

SELECT setval(pg_get_serial_sequence('landlord', 'id'), (SELECT MAX(id) FROM landlord));

INSERT INTO property (id, status, is_active, property_build_type, address_id)
VALUES (1, 1, true, 1, 1);

SELECT setval(pg_get_serial_sequence('property', 'id'), (SELECT MAX(id) FROM property));

INSERT INTO property_ownership (id, is_active, occupancy_type, landlord_type, ownership_type, current_num_households,
                                current_num_tenants, registration_number_id, primary_landlord_id, property_id)
VALUES (1, true, 0, 0, 1, 1, 2, 6, 1, 1);

SELECT setval(pg_get_serial_sequence('property_ownership', 'id'), (SELECT MAX(id) FROM property_ownership));