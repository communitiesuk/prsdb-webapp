INSERT INTO prsdb_user (id, created_date)
VALUES ('urn:fdc:gov.uk:2022:LANDLORD1', '10/14/24'),
       ('urn:fdc:gov.uk:2022:LANDLORD2', '10/14/24'),
       ('urn:fdc:gov.uk:2022:LANDLORD3', '10/14/24'),
       ('urn:fdc:gov.uk:2022:LANDLORD4', '10/14/24'),
       ('urn:fdc:gov.uk:2022:LANDLORD5', '10/14/24'),
       ('urn:fdc:gov.uk:2022:LANDLORD6', '10/14/24');

INSERT INTO registration_number (id, created_date, number, type)
VALUES (1, '09/13/24', 2001001001, 1),
       (2, '09/13/24', 2001001002, 1),
       (3, '09/13/24', 2001001003, 1),
       (4, '09/13/24', 2001001004, 1),
       (5, '09/13/24', 2001001005, 1),
       (6, '09/13/24', 2001001006, 1),
       (7, '09/13/24', 1001001001, 0),
       (8, '09/13/24', 1001001002, 0);
SELECT setval(pg_get_serial_sequence('registration_number', 'id'), (SELECT MAX(id) FROM registration_number));

INSERT INTO address (id, created_date, last_modified_date, uprn, single_line_address, local_council_id, postcode)
VALUES (1, '09/13/24', '09/13/24', 1, '1 Fictional Road', 2, 'EG1 1EG'),
       (2, '09/13/24', '09/13/24', 2, '2 Fake Way', 2, 'EG1 1EG'),
       (3, '09/13/24', '09/13/24', 3, '3 Imaginary Street', 2, 'EG1 1EG');
SELECT setval(pg_get_serial_sequence('address', 'id'), (SELECT MAX(id) FROM address));

INSERT INTO landlord (id, created_date, last_modified_date, registration_number_id, individual_address_id, individual_date_of_birth,
                      is_active, last_deactivated_date, has_registered_property, individual_phone_number, individual_subject_identifier,
                      individual_name, individual_email, individual_country_of_residence, individual_is_verified,
                      individual_has_accepted_privacy_notice)
VALUES (1, current_date - interval '3 months' - interval '1 day' + interval '12 hours', '09/13/24', 1, 1, '09/13/2000',
        true, null, false, 07111111111, 'urn:fdc:gov.uk:2022:LANDLORD1', 'Old No Property', 'landlord1@example.com',
        'England or Wales', true, true),
       (2, current_date - interval '3 months' + interval '12 hours', '09/13/24', 2, 1, '09/13/2000',
        true, null, false, 07111111112, 'urn:fdc:gov.uk:2022:LANDLORD2', 'Boundary No Property', 'landlord2@example.com',
        'England or Wales', true, true),
       (3, current_date - interval '1 year', '09/13/24', 3, 1, '09/13/2000',
        false, '01/01/26', false, 07111111113, 'urn:fdc:gov.uk:2022:LANDLORD3', 'Already Inactive', 'landlord3@example.com',
        'England or Wales', true, true),
       (4, current_date - interval '1 day' + interval '12 hours', '09/13/24', 4, 1, '09/13/2000',
        true, null, true, 07111111114, 'urn:fdc:gov.uk:2022:LANDLORD4', 'Only Inactive Property', 'landlord4@example.com',
        'England or Wales', true, true),
       (5, current_date - interval '1 year', '09/13/24', 5, 1, '09/13/2000',
        true, null, true, 07111111115, 'urn:fdc:gov.uk:2022:LANDLORD5', 'Has Active Property', 'landlord5@example.com',
        'England or Wales', true, true),
       (6, current_date - interval '1 year', '09/13/24', 6, 1, '09/13/2000',
        true, null, false, 07111111116, 'urn:fdc:gov.uk:2022:LANDLORD6', 'Legacy Flag Unset', 'landlord6@example.com',
        'England or Wales', true, true);
SELECT setval(pg_get_serial_sequence('landlord', 'id'), (SELECT MAX(id) FROM landlord));

INSERT INTO property_ownership (id, is_active, ownership_type_before_pdjb955, ownership_type, current_num_households, current_num_tenants,
                                registration_number_id, address_id, property_build_type, num_bedrooms, rent_amount,
                                marked_joint_landlord, is_occupied, correspondence_email, correspondence_address_id, renewal_date, paid_for_until)
VALUES (1, false, 1, 1, 0, 0, 7, 2, 1, null, null, true, false, 'email@example.com', 1, current_date + 365, current_date + 365),
       (2, true, 1, 1, 0, 0, 8, 3, 1, null, null, true, false, 'email@example.com', 1, current_date + 365, current_date + 365);
SELECT setval(pg_get_serial_sequence('property_ownership', 'id'), (SELECT MAX(id) FROM property_ownership));

INSERT INTO ownership_link (landlord_id, landlordship_id, created_date)
VALUES (4, 1, '2025-01-15'),
       (5, 1, '2025-01-15'),
       (5, 2, '2025-01-15'),
       (6, 2, '2025-01-15');
