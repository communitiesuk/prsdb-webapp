INSERT INTO address (id, created_date, single_line_address, postcode)
VALUES (9001, current_date, '10 Trustee Street, EG1 2AA', 'EG1 2AA'),
       (9002, current_date, '11 Trustee Street, EG1 2AA', 'EG1 2AA'),
       (9003, current_date, '12 Trustee Street, EG1 2AA', 'EG1 2AA');
SELECT setval(pg_get_serial_sequence('address', 'id'), (SELECT MAX(id) FROM address));

UPDATE landlord
SET organisation_is_company = false,
    organisation_company_number = NULL,
    organisation_is_trust = true,
    organisation_lead_trustee_name = 'Lena Trustee',
    organisation_lead_trustee_date_of_birth = '1980-06-15',
    organisation_lead_trustee_email = 'lena.trustee@example.com',
    organisation_lead_trustee_phone = '07123456789',
    organisation_lead_trustee_address_id = 9001
WHERE id = 36;

INSERT INTO organisation_governing_body_member (created_date, organisation_landlord_id, type, name, date_of_birth, address_id)
VALUES (current_date, 36, 1, 'Tara Trustee', '1974-03-18', 9002),
       (current_date, 36, 1, 'Tom Trustee', '1981-09-02', 9003);
