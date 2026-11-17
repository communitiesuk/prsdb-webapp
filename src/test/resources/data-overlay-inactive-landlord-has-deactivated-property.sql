-- Gives the mock user's landlord (id 1) a deactivated property at 2 Fake Way.
-- Apply on top of data-mockuser-inactive-landlord.sql.

INSERT INTO registration_number (id, created_date, number, type)
VALUES (2, '3/26/25', 1001001001, 0);
SELECT setval(pg_get_serial_sequence('registration_number', 'id'), (SELECT MAX(id) FROM registration_number));

INSERT INTO address (id, created_date, last_modified_date, uprn, single_line_address, local_council_id, postcode)
VALUES (2, '09/13/24', '09/13/24', 2, '2 Fake Way', 2, 'EG1 1EG');
SELECT setval(pg_get_serial_sequence('address', 'id'), (SELECT MAX(id) FROM address));

INSERT INTO property_ownership (id, is_active, ownership_type_before_pdjb955, ownership_type, current_num_households, current_num_tenants,
                                registration_number_id, address_id, property_build_type, license_id, num_bedrooms, bills_included_list,
                                custom_bills_included, furnished_status, rent_frequency, custom_rent_frequency, rent_amount, is_occupied,
                                correspondence_email, correspondence_address_id, renewal_date)
VALUES (1, false, 1, 1, 0, 0, 2, 2, 1, null, null, null, null, null, null, null, null, false, 'email@example.com', 1, current_date + 365);
SELECT setval(pg_get_serial_sequence('property_ownership', 'id'), (SELECT MAX(id) FROM property_ownership));

INSERT INTO ownership_link (landlord_id, landlordship_id, created_date)
VALUES (1, 1, '2025-01-15');
