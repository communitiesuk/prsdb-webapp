INSERT INTO prsdb_user (id, created_date)
VALUES ('urn:fdc:gov.uk:2022:UVWXY', '10/14/24');

INSERT INTO registration_number (id, created_date, number, type)
VALUES (1, '09/13/24', 2001001001, 1);
SELECT setval(pg_get_serial_sequence('registration_number', 'id'), (SELECT MAX(id) FROM registration_number));

INSERT INTO address (id, created_date, last_modified_date, uprn, single_line_address, local_council_id, postcode)
VALUES (1, '09/13/24', '09/13/24', 1, '5 Meadow Lane, Sampleton, MB1 2CD', 2, 'MB1 2CD');
SELECT setval(pg_get_serial_sequence('address', 'id'), (SELECT MAX(id) FROM address));

INSERT INTO landlord (id, created_date, last_modified_date, landlord_type, registration_number_id,
                      organisation_landlord_name, organisation_address_id, organisation_email, organisation_phone_number,
                      organisation_is_company, organisation_is_charity, organisation_is_trust, organisation_company_number,
                      organisation_registrant_name, organisation_registrant_date_of_birth,
                      organisation_registrant_email, organisation_registrant_phone_number,
                      organisation_main_contact_name, organisation_main_contact_email, organisation_main_contact_phone)
VALUES (1, '09/13/24', '09/13/24', 1, 1,
        'Meadowbrook Housing Ltd', 1, 'contact@meadowbrook.example', '01234567890',
        true, false, false, '09876543',
        'Riya Registrant', '1985-06-15', 'riya.registrant@meadowbrook.example', '01444444444',
        'Sam Main-Contact', 'sam.maincontact@meadowbrook.example', '02555555555');
SELECT setval(pg_get_serial_sequence('landlord', 'id'), (SELECT MAX(id) FROM landlord));

INSERT INTO organisational_landlord_user (id, organisation_landlord_id, subject_identifier, name, email, created_date)
VALUES (1, 1, 'urn:fdc:gov.uk:2022:UVWXY', 'Riya Registrant', 'riya.registrant@meadowbrook.example', current_date);
SELECT setval(pg_get_serial_sequence('organisational_landlord_user', 'id'), (SELECT MAX(id) FROM organisational_landlord_user));
