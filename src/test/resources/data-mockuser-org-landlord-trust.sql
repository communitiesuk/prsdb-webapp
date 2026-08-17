INSERT INTO prsdb_user (id, created_date)
VALUES ('urn:fdc:gov.uk:2022:ORG01', '10/14/24');

INSERT INTO registration_number (id, created_date, number, type)
VALUES (1, '09/13/24', 2001001001, 1);
SELECT setval(pg_get_serial_sequence('registration_number', 'id'), (SELECT MAX(id) FROM registration_number));

INSERT INTO address (id, created_date, last_modified_date, uprn, single_line_address, local_council_id, postcode)
VALUES (1, '09/13/24', '09/13/24', 1, 'Keystone House, 1 Fictional Road, EG1 1EG', 2, 'EG1 1EG'),
       (2, '09/13/24', '09/13/24', 2, '3rd Floor, 88 Kingsway Square, London, ZX1 4GP', 2, 'ZX1 4GP'),
       (3, '09/13/24', '09/13/24', 3, '12 Director Avenue, EG2 2EG', 2, 'EG2 2EG'),
       (4, '09/13/24', '09/13/24', 4, '34 Partner Lane, EG3 3EG', 2, 'EG3 3EG'),
       (5, '09/13/24', '09/13/24', 1013, '1 PRSDB Square, EG1 2AA', 1, 'EG1 2AA'),
       (6, '09/13/24', '09/13/24', 1014, '2 PRSDB Square, EG1 2AA', 1, 'EG1 2AA'),
       (7, '09/13/24', '09/13/24', 1015, '3 PRSDB Square, EG1 2AA', 1, 'EG1 2AA');
SELECT setval(pg_get_serial_sequence('address', 'id'), (SELECT MAX(id) FROM address));

INSERT INTO landlord (id, created_date, last_modified_date, landlord_type, registration_number_id,
                      organisation_landlord_name, organisation_address_id, organisation_email, organisation_phone_number,
                      organisation_is_company, organisation_is_charity, organisation_is_trust,
                      organisation_registrant_name, organisation_registrant_date_of_birth,
                      organisation_registrant_email, organisation_registrant_phone_number,
                      organisation_main_contact_name, organisation_main_contact_email, organisation_main_contact_phone,
                      organisation_lead_trustee_name, organisation_lead_trustee_date_of_birth,
                      organisation_lead_trustee_email, organisation_lead_trustee_phone, organisation_lead_trustee_address_id)
VALUES (1, '09/13/24', '09/13/24', 1, 1,
        'Keystone Living', 1, 'contact@keystoneliving.com', '01234567890',
        false, false, true,
        'Priya Registrant', '1990-01-01', 'priya.registrant@keystoneliving.com', '01111111111',
        'Sam Main-Contact', 'sam.maincontact@keystoneliving.com', '02222222222',
        'Anita Locke', '2001-03-08', 'anita.locke@keystoneliving.com', '03333333333', 2);
SELECT setval(pg_get_serial_sequence('landlord', 'id'), (SELECT MAX(id) FROM landlord));

INSERT INTO organisational_landlord_user (id, organisation_landlord_id, subject_identifier, name, email, created_date)
VALUES (1, 1, 'urn:fdc:gov.uk:2022:ORG01', 'Priya Registrant', 'priya.registrant@keystoneliving.com', current_date);
SELECT setval(pg_get_serial_sequence('organisational_landlord_user', 'id'), (SELECT MAX(id) FROM organisational_landlord_user));

INSERT INTO organisation_governing_body_member (id, created_date, organisation_landlord_id, type, name, date_of_birth, address_id)
VALUES (1, current_date, 1, 0, 'David Director', '1974-03-18', 3),
       (2, current_date, 1, 2, 'Omar Hassan', '2001-03-08', 4);
SELECT setval(pg_get_serial_sequence('organisation_governing_body_member', 'id'), (SELECT MAX(id) FROM organisation_governing_body_member));
