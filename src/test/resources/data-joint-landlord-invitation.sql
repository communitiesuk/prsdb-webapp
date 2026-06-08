INSERT INTO prsdb_user (id, created_date)
VALUES ('urn:fdc:gov.uk:2022:UVWXY', '10/14/24'),
       ('urn:fdc:gov.uk:2022:ABCDE', '10/14/24');

INSERT INTO registration_number (id, created_date, number, type)
VALUES (1, '09/13/24', 2001001001, 1),
       (2, '09/13/24', 1001001001, 0),
       (3, '09/13/24', 3001001001, 1);
SELECT setval(pg_get_serial_sequence('registration_number', 'id'), (SELECT MAX(id) FROM registration_number));

INSERT INTO address (id, created_date, last_modified_date, uprn, single_line_address, local_council_id, postcode)
VALUES (1, '09/13/24', '09/13/24', 1, '1 Fictional Road', 2, 'EG1 1EG'),
       (2, '09/13/24', '09/13/24', 2, '2 Fake Way', 2, 'EG1 1EG');
SELECT setval(pg_get_serial_sequence('address', 'id'), (SELECT MAX(id) FROM address));

INSERT INTO landlord (id, created_date, last_modified_date, registration_number_id, address_id, date_of_birth,
                      is_active, phone_number, subject_identifier, name, email, country_of_residence, is_verified, has_accepted_privacy_notice)
VALUES (1, '09/13/24', '09/13/24', 1, 1, '09/13/2000', true, 07111111111, 'urn:fdc:gov.uk:2022:UVWXY',
        'Invited User', 'invited.user@example.com', 'England or Wales', false, true),
       (2, '09/13/24', '09/13/24', 3, 1, '09/13/2000', true, 07111111111, 'urn:fdc:gov.uk:2022:ABCDE',
        'Original Landlord', 'original.landlord@example.com', 'England or Wales', false, true);
SELECT setval(pg_get_serial_sequence('landlord', 'id'), (SELECT MAX(id) FROM landlord));

INSERT INTO property_ownership (id, is_active, ownership_type, current_num_households, current_num_tenants,
                                registration_number_id, address_id, created_date, property_build_type,
                                num_bedrooms, bills_included_list, custom_bills_included, furnished_status,
                                rent_frequency, custom_rent_frequency, rent_amount)

VALUES (1, true, 1, 1, 2, 2, 2, current_date, 1,
        1, null, null, 2, 1, null, 123.12);
SELECT setval(pg_get_serial_sequence('property_ownership', 'id'), (SELECT MAX(id) FROM property_ownership));

INSERT INTO landlordship_members (landlord_id, landlordship_id)
VALUES (1, 1);

INSERT INTO joint_landlord_invitation (id, invited_email, registered_propertyid, token, inviting_landlord_id, created_date)
VALUES (1, 'invited@example.com', 1, 'aaaabbbb-cccc-dddd-eeee-ffff00001111', 2,current_date
       );
SELECT setval(pg_get_serial_sequence('joint_landlord_invitation', 'id'), (SELECT MAX(id) FROM joint_landlord_invitation));
