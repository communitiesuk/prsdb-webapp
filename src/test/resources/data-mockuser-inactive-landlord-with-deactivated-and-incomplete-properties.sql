INSERT INTO prsdb_user (id, created_date)
VALUES ('urn:fdc:gov.uk:2022:UVWXY', '10/14/24');

INSERT INTO registration_number (id, created_date, number, type)
VALUES (1, '09/13/24', 2001001001, 1),
       (2, '3/26/25', 1001001001, 0);
SELECT setval(pg_get_serial_sequence('registration_number', 'id'), (SELECT MAX(id) FROM registration_number));

INSERT INTO address (id, created_date, last_modified_date, uprn, single_line_address, local_council_id, postcode)
VALUES (1, '09/13/24', '09/13/24', 1, '1 Fictional Road', 2, 'EG1 1EG'),
       (2, '09/13/24', '09/13/24', 2, '2 Fake Way', 2, 'EG1 1EG');
SELECT setval(pg_get_serial_sequence('address', 'id'), (SELECT MAX(id) FROM address));

INSERT INTO landlord (id, created_date, last_modified_date, registration_number_id, individual_address_id, individual_date_of_birth,
                      is_active, last_deactivated_date, individual_phone_number, individual_subject_identifier, individual_name, individual_email,
                      individual_country_of_residence, individual_is_verified, individual_has_accepted_privacy_notice)
VALUES (1, '09/13/24', '09/13/24', 1, 1, '09/13/2000', false, '01/01/26', 07111111111, 'urn:fdc:gov.uk:2022:UVWXY',
        'Alexander Smith', 'alex.surname@example.com', 'England or Wales', true, true);
SELECT setval(pg_get_serial_sequence('landlord', 'id'), (SELECT MAX(id) FROM landlord));

INSERT INTO saved_journey_state (id, created_date, last_modified_date, journey_id, serialized_state, subject_identifier)
VALUES (1, current_date, current_date, 'example-incomplete-journey1', '{}', 'urn:fdc:gov.uk:2022:UVWXY');

INSERT INTO landlord_incomplete_properties (user_id, saved_journey_state_id)
VALUES ('urn:fdc:gov.uk:2022:UVWXY', 1);

INSERT INTO property_ownership (id, is_active, ownership_type_before_pdjb955, ownership_type, current_num_households, current_num_tenants,
                                registration_number_id, address_id, property_build_type, license_id, num_bedrooms, bills_included_list,
                                custom_bills_included, furnished_status, rent_frequency, custom_rent_frequency, rent_amount, is_occupied,
                                correspondence_email, correspondence_address_id, renewal_date)
VALUES (1, false, 1, 1, 0, 0, 2, 2, 1, null, null, null, null, null, null, null, null, false, 'email@example.com', 1, current_date + 365);
SELECT setval(pg_get_serial_sequence('property_ownership', 'id'), (SELECT MAX(id) FROM property_ownership));

INSERT INTO ownership_link (landlord_id, landlordship_id, created_date)
VALUES (1, 1, '2025-01-15');
