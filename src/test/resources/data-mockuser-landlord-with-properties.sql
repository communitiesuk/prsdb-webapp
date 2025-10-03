INSERT INTO one_login_user (id, created_date)
VALUES ('urn:fdc:gov.uk:2022:UVWXY', '10/14/24');

INSERT INTO registration_number (id, created_date, number, type)
VALUES (1, '09/13/24', 2001001001, 1),
       (2, '3/26/25', 1001001001, 0),
       (3, '3/26/25', 1001001002, 0),
       (4, '3/26/25', 1001001003, 0);
SELECT setval(pg_get_serial_sequence('registration_number', 'id'), (SELECT MAX(id) FROM registration_number));

INSERT INTO address (id, created_date, last_modified_date, uprn, single_line_address, local_authority_id, postcode)
VALUES  (1, '09/13/24', '09/13/24', 1, '1 Fictional Road', 2, 'EG1 1EG'),
        (2, '09/13/24', '09/13/24', 2, '2 Fake Way', 2, 'EG1 1EG'),
        (3, '09/13/24', '09/13/24', 3, '3 Imaginary Street', 2, 'EG1 1EG'),
        (4, '09/13/24', '09/13/24', 4, '4 Pretend Crescent', 2, 'EG1 1EG');
SELECT setval(pg_get_serial_sequence('address', 'id'), (SELECT MAX(id) FROM address));

INSERT INTO landlord (id, created_date, last_modified_date, registration_number_id, address_id, date_of_birth,
                      is_active, phone_number, subject_identifier, name, email, country_of_residence, is_verified, has_accepted_privacy_notice)
VALUES (1, '09/13/24', '09/13/24', 1, 1, '09/13/2000', true, 07111111111, 'urn:fdc:gov.uk:2022:UVWXY',
        'Alexander Smith', 'alex.surname@example.com', 'England or Wales', false, true);
SELECT setval(pg_get_serial_sequence('landlord', 'id'), (SELECT MAX(id) FROM landlord));


INSERT INTO property (id, status, is_active, property_build_type, address_id)
VALUES (1, 1, true, 1, 2),
       (2, 1, true, 1, 3),
       (3, 1, true, 1, 4);
SELECT setval(pg_get_serial_sequence('property', 'id'), (SELECT MAX(id) FROM property));

INSERT INTO property_ownership (id, is_active, occupancy_type, ownership_type, current_num_households,
                                current_num_tenants,
                                registration_number_id, primary_landlord_id, property_id, created_date)
VALUES (1, true, 0, 1, 1, 2, 2, 1, 1, '3/26/25'),
       (2, true, 0, 1, 1, 2, 3, 1, 2, '3/26/25'),
       (3, true, 0, 1, 1, 2, 4, 1, 3, '3/26/25');

INSERT INTO file_upload (id, created_date, status, object_key, e_tag, version_id, extension)
VALUES (1, '09/13/24', 1, 'file-key-123', 'e-tag-123', 'version-id-123', 'pdf');

INSERT INTO certificate_upload (id, created_date, file_upload_id, property_ownership_id, category)
VALUES (1, '09/13/24', 1, 1, 1);