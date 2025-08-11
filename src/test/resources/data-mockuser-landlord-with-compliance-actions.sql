INSERT INTO one_login_user (id, created_date)
VALUES ('urn:fdc:gov.uk:2022:UVWXY', '10/14/24');

INSERT INTO registration_number (id, created_date, number, type)
VALUES (1, '09/13/24', 2001001001, 1),
       (2, '3/26/25', 0006001004, 0),
       (3, '3/26/25', 7006001006, 0),
       (4, '3/26/25', 0006001008, 0);
SELECT setval(pg_get_serial_sequence('registration_number', 'id'), (SELECT MAX(id) FROM registration_number));

INSERT INTO address (id, created_date, last_modified_date, uprn, single_line_address, local_authority_id)
VALUES  (1, '09/13/24', '09/13/24', 1, '1 Fictional Road', 2),
        (2, '09/13/24', '09/13/24', 2, '2 Fake Way', 2),
        (3, '09/13/24', '09/13/24', 3, '3 Imaginary Street', 2),
        (4, '09/13/24', '09/13/24', 4, '4 Pretend Crescent', 2);
SELECT setval(pg_get_serial_sequence('address', 'id'), (SELECT MAX(id) FROM address));

INSERT INTO landlord (id, created_date, last_modified_date, registration_number_id, address_id, date_of_birth,
                      is_active, phone_number, subject_identifier, name, email, country_of_residence, is_verified)
VALUES (1, '09/13/24', '09/13/24', 1, 1, '09/13/2000', true, 07111111111, 'urn:fdc:gov.uk:2022:UVWXY',
        'Alexander Smith', 'alex.surname@example.com', 'England or Wales', false);
SELECT setval(pg_get_serial_sequence('landlord', 'id'), (SELECT MAX(id) FROM landlord));

INSERT INTO form_context (id, created_date, last_modified_date, journey_type, context, subject_identifier)
VALUES (1, current_date, current_date, 7, '{"gas-safety-certificate":{"hasCert":true},"gas-safety-certificate-issue-date":{"day":"4","month":"1","year":"2000"},' ||
                                          '"gas-safety-certificate-outdated":{},"eicr":{"hasCert":false},"eicr-exemption":{"hasExemption":true},"eicr-exemption-reason":{"exemptionReason":"STUDENT_ACCOMMODATION"},' ||
                                          '"eicr-exemption-confirmation":{},"epc":{"hasCert":"NO"},"epc-missing":{}}','urn:fdc:gov.uk:2022:UVWXY'),
       (2, current_date, current_date, 7, '{}','urn:fdc:gov.uk:2022:UVWXY');

INSERT INTO property (id, status, is_active, property_build_type, address_id)
VALUES (1, 1, true, 1, 2),
       (2, 1, true, 1, 3),
       (3, 1, true, 1, 4);
SELECT setval(pg_get_serial_sequence('property', 'id'), (SELECT MAX(id) FROM property));

INSERT INTO property_ownership (id, is_active, occupancy_type, ownership_type, current_num_households,
                                current_num_tenants,
                                registration_number_id, primary_landlord_id, property_id, created_date, incomplete_compliance_form_id)
VALUES (1, true, 0, 1, 1, 2, 2, 1, 1, current_date, 1),
       (2, true, 0, 1, 1, 2, 3, 1, 2, current_date, 2),
       (3, true, 0, 1, 1, 2, 4, 1, 3, current_date, null);

INSERT INTO property_compliance (id, property_ownership_id, created_date, last_modified_date,
                                 gas_safety_upload_id, gas_safety_cert_issue_date, gas_safety_cert_engineer_num, gas_safety_cert_exemption_reason, gas_safety_cert_exemption_other_reason,
                                 eicr_id, eicr_issue_date, eicr_exemption_reason, eicr_exemption_other_reason,
                                 epc_url, epc_expiry_date, tenancy_started_before_epc_expiry, epc_energy_rating, epc_exemption_reason, epc_mees_exemption_reason,
                                 has_fire_safety_declaration, has_keep_property_safe_declaration, has_responsibility_to_tenants_declaration)
VALUES  (1, 3, '01/01/25', '01/01/25',
         null, null, null, null, null,
         null, null, 1, null,
         'https://find-energy-certificate-staging.digital.communities.gov.uk/energy-certificate/0000-0000-0000-1050-2867', '2013-02-28', false, 'g', null, null,
         true, true, true);