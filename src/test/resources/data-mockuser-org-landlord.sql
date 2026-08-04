INSERT INTO registration_number (id, created_date, number, type)
VALUES (1, '09/13/24', 3001001001, 1);

SELECT setval(pg_get_serial_sequence('registration_number', 'id'), (SELECT MAX(id) FROM registration_number));

INSERT INTO landlord (id, created_date, last_modified_date, registration_number_id, landlord_type,
                      organisation_landlord_name, organisation_address_id, organisation_email,
                      organisation_phone_number, organisation_registrant_name,
                      organisation_registrant_date_of_birth, organisation_registrant_email,
                      organisation_registrant_phone_number, organisation_is_company,
                      organisation_is_charity, organisation_is_trust, organisation_company_number,
                      organisation_main_contact_name, organisation_main_contact_email,
                      organisation_main_contact_phone)
VALUES (1, '09/13/24', '09/13/24', 1, 1, 'Test Organisation Name', 1,
        'org@test.com', '07123456789', 'Registrant Name', '01/01/1990',
        'registrant@example.com', '07111111111', true, false, false, '12345678',
        'Main Contact', 'main-contact@example.com', '07111111112');

INSERT INTO organisation_landlord_user (organisation_landlord_id, subject_identifier, name, email, created_date)
VALUES (1, 'urn:fdc:gov.uk:2022:UVWXY', 'Registrant Name', 'registrant@example.com', '09/13/24');

SELECT setval(pg_get_serial_sequence('landlord', 'id'), (SELECT MAX(id) FROM landlord));
