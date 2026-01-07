INSERT INTO one_login_user (id, created_date)
VALUES ('urn:fdc:gov.uk:2022:UVWXY', '10/14/24');

INSERT INTO registration_number (id, created_date, number, type)
VALUES (1, '09/13/24', 2001001001, 1);

INSERT INTO address (id, created_date, last_modified_date, uprn, single_line_address, local_council_id, postcode)
VALUES  (1, '09/13/24', '09/13/24', 1, '1 Fictional Road', 2, 'EG1 1EG');

INSERT INTO landlord (id, created_date, last_modified_date, registration_number_id, address_id, date_of_birth,
                      is_active, phone_number, subject_identifier, name, email, country_of_residence, is_verified, has_accepted_privacy_notice)
VALUES (1, '09/13/24', '09/13/24', 1, 1, '09/13/2000', true, 07111111111, 'urn:fdc:gov.uk:2022:UVWXY',
        'Alexander Smith', 'alex.surname@example.com', 'England or Wales', false, true);
SELECT setval(pg_get_serial_sequence('landlord', 'id'), (SELECT MAX(id) FROM landlord));

INSERT INTO form_context (id, created_date, last_modified_date, journey_type, context, subject_identifier)
VALUES (1, current_date, current_date,3, '{"lookup-address":{"houseNameOrNumber":"1","postcode":"WC2R 1LA"},"looked-up-addresses":"[{\"singleLineAddress\":\"1, SAVOY COURT, LONDON, WC2R 0EX\",\"localCouncilId\":318,\"uprn\":100023432931,\"buildingNumber\":\"1\",\"streetName\":\"SAVOY COURT\",\"townName\":\"LONDON\",\"postcode\":\"WC2R 0EX\"}]","select-address":{"address":"1, SAVOY COURT, LONDON, WC2R 0EX"},"property-type":{"customPropertyType":","propertyType":"DETACHED_HOUSE"}}','urn:fdc:gov.uk:2022:UVWXY');

INSERT INTO saved_journey_state (id, created_date, last_modified_date, journey_id, serialized_state, subject_identifier)
VALUES (1, current_date,current_date, '1', '{"journeyData":{"lookup-address":{"houseNameOrNumber":"1","postcode":"WC2R1LA"},"select-address":{"address":"1, SAVOY COURT, LONDON, WC2R 0EX"},"property-type":{"customPropertyType":"","propertyType":"DETACHED_HOUSE"}},"cachedAddresses":"[{\"singleLineAddress\":\"1, SAVOY COURT, LONDON, WC2R 0EX\",\"localCouncilId\":1,\"uprn\":1038,\"buildingNumber\":\"1\",\"streetName\":\"SAVOYCOURT\",\"townName\":\"LONDON\",\"postcode\":\"WC2R0EX\"}]","isAddressAlreadyRegistered":"false"}', 'urn:fdc:gov.uk:2022:UVWXY');

INSERT INTO landlord_incomplete_properties (landlord_id, saved_journey_state_id)
VALUES (1, 1);
