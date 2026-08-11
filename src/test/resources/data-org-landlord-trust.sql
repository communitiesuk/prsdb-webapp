INSERT INTO prsdb_user (id, created_date)
VALUES ('urn:fdc:gov.uk:2022:ORG01', '2026-07-23');

INSERT INTO registration_number (id, created_date, number, type)
VALUES (1, '2026-07-23', 2001001999, 1);

SELECT setval(pg_get_serial_sequence('registration_number', 'id'), (SELECT MAX(id) FROM registration_number));

INSERT INTO address (id, created_date, last_modified_date, uprn, single_line_address, local_council_id, postcode)
VALUES (1, '2026-07-23', '2026-07-23', 1, '1 PRSDB Square, EG1 2AA', 2, 'EG1 2AA'),
       (2, '2026-07-23', '2026-07-23', 2, '2 PRSDB Square, EG1 2AA', 2, 'EG1 2AA');

SELECT setval(pg_get_serial_sequence('address', 'id'), (SELECT MAX(id) FROM address));

INSERT INTO landlord (id, created_date, last_modified_date, registration_number_id, landlord_type,
                      organisation_landlord_name, organisation_address_id, organisation_email,
                      organisation_phone_number, organisation_registrant_name,
                      organisation_registrant_date_of_birth, organisation_registrant_email,
                      organisation_registrant_phone_number, organisation_is_company,
                      organisation_is_charity, organisation_is_trust, organisation_company_number,
                      organisation_lead_trustee_name, organisation_lead_trustee_date_of_birth,
                      organisation_lead_trustee_email, organisation_lead_trustee_phone,
                      organisation_lead_trustee_address_id, organisation_main_contact_name,
                      organisation_main_contact_email, organisation_main_contact_phone)
VALUES (1, '2026-07-23', '2026-07-23', 1, 1, 'Local Organisation Landlord', 1,
        'local-trust-org-landlord@example.com', '07111111111', 'Local Registrant', '1990-01-01',
        'local-registrant@example.com', '07111111112', false, false, true, null,
        'Existing Lead Trustee', '1980-06-15', 'existing-trustee@example.com', '07123456789', 2,
        'Local Main Contact', 'local-main-contact@example.com', '07111111113');

SELECT setval(pg_get_serial_sequence('landlord', 'id'), (SELECT MAX(id) FROM landlord));

INSERT INTO organisational_landlord_user (organisation_landlord_id, subject_identifier, name, email, created_date)
VALUES (1, 'urn:fdc:gov.uk:2022:ORG01', 'Local Registrant', 'local-registrant@example.com', '2026-07-23');
