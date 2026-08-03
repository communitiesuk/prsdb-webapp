INSERT INTO prsdb_user (id, created_date)
VALUES ('urn:fdc:gov.uk:2022:UVWXY', '10/14/24');

INSERT INTO registration_number (id, created_date, number, type)
VALUES (1, '09/13/24', 2001001001, 1);
SELECT setval(pg_get_serial_sequence('registration_number', 'id'), (SELECT MAX(id) FROM registration_number));

INSERT INTO address (id, created_date, last_modified_date, uprn, single_line_address, local_council_id, postcode)
VALUES (1, '09/13/24', '09/13/24', 1, '3rd Floor, 88 Kingsway Square, London, ZX1 4QP', 2, 'ZX1 4QP');
SELECT setval(pg_get_serial_sequence('address', 'id'), (SELECT MAX(id) FROM address));

INSERT INTO landlord (id, created_date, last_modified_date, landlord_type, registration_number_id,
                      organisation_landlord_name, organisation_address_id, organisation_email,
                      organisation_phone_number, organisation_is_company, organisation_is_charity,
                      organisation_is_trust, organisation_company_number,
                      organisation_main_contact_name, organisation_main_contact_email, organisation_main_contact_phone)
VALUES (1, '09/13/24', '09/13/24', 1, 1,
        'Keystone Living Group', 1, 'hello@keystoneliving.co.uk',
        '020 7123 4567', true, false, false, '01234567',
        'Jane Doe', 'jane.doe@keystoneliving.co.uk', '020 7123 4568');
SELECT setval(pg_get_serial_sequence('landlord', 'id'), (SELECT MAX(id) FROM landlord));

INSERT INTO organisation_landlord_user (organisation_landlord_id, subject_identifier, created_date)
VALUES (1, 'urn:fdc:gov.uk:2022:UVWXY', '09/13/24');
