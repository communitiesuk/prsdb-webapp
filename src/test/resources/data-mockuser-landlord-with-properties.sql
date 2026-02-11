INSERT INTO one_login_user (id, created_date)
VALUES ('urn:fdc:gov.uk:2022:UVWXY', '10/14/24');

INSERT INTO passcode (passcode, local_council_id, subject_identifier)
VALUES ('PRS23', 2, 'urn:fdc:gov.uk:2022:UVWXY');

INSERT INTO registration_number (id, created_date, number, type)
VALUES (1, '09/13/24', 2001001001, 1),
       (2, '3/26/25', 1001001001, 0),
       (3, '3/26/25', 1001001002, 0),
       (4, '3/26/25', 1001001003, 0);
SELECT setval(pg_get_serial_sequence('registration_number', 'id'), (SELECT MAX(id) FROM registration_number));

INSERT INTO address (id, created_date, last_modified_date, uprn, single_line_address, local_council_id, postcode)
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

INSERT INTO form_context (id, journey_type, context, subject_identifier)
VALUES (1, 7, '{}','urn:fdc:gov.uk:2022:UVWXY');
SELECT setval(pg_get_serial_sequence('form_context', 'id'), (SELECT MAX(id) FROM form_context));

INSERT INTO saved_journey_state (id, created_date, journey_id, serialized_state, subject_identifier)
VALUES (1,current_date, 'example-incomplete-journey1', '{}', 'urn:fdc:gov.uk:2022:UVWXY');

INSERT INTO license (id, license_type, license_number)
VALUES (1, 1, 'LIC123456');
SELECT setval(pg_get_serial_sequence('license', 'id'), (SELECT MAX(id) FROM license));

INSERT INTO property_ownership (id, is_active, ownership_type, current_num_households, current_num_tenants, registration_number_id, primary_landlord_id, address_id, property_build_type, license_id, incomplete_compliance_form_id,
                                num_bedrooms, bills_included_list, custom_bills_included, furnished_status, rent_frequency, custom_rent_frequency, rent_amount)
VALUES (1, true, 1, 0, 0, 2, 1, 2, 1, 1, 1,
        null, null, null, null, null, null, null),
       (2, true, 1, 1, 2, 3, 1, 3, 1, null, null,
        1, null, null, 2, 1, null, 123.12),
       (3, true, 1, 1, 2, 4, 1, 4, 1, null, null,
        1, null, null, 2, 1, null, 123.12);

INSERT INTO file_upload (id, created_date, status, object_key, e_tag, version_id, extension)
VALUES (1, '09/13/24', 1, 'file-key-123', 'e-tag-123', 'version-id-123', 'pdf');

INSERT INTO certificate_upload (id, created_date, file_upload_id, property_ownership_id, category)
VALUES (1, '09/13/24', 1, 1, 1);

INSERT INTO property_compliance (id, property_ownership_id, gas_safety_upload_id, gas_safety_cert_issue_date, gas_safety_cert_engineer_num, eicr_exemption_reason, epc_exemption_reason)
VALUES (1, 1,1, current_date, '1234567', 0, 0);

