INSERT INTO prsdb_user (id, created_date)
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

INSERT INTO saved_journey_state (id, created_date, last_modified_date, journey_id, serialized_state, subject_identifier)
VALUES (1, current_date-4, current_date-4, '1', '{"journeyData":{"lookup-address":{"houseNameOrNumber":"1","postcode":"NW51tl"},"select-address":{"address":"1, Example Road, EG"},"property-type":{"customPropertyType":"","propertyType":"FLAT"}},"cachedAddresses":"[{\"singleLineAddress\":\"1, Example Road, EG\",\"localCouncilId\":2,\"uprn\":1123456,\"buildingNumber\":\"1\",\"postcode\":\"EG\"}]","isAddressAlreadyRegistered":"false"}', 'urn:fdc:gov.uk:2022:UVWXY'),
       (2, current_date-3, current_date-3, '2', '{"journeyData":{"lookup-address":{"houseNameOrNumber":"2","postcode":"NW51tl"},"select-address":{"address":"2, Example Road, EG"},"property-type":{"customPropertyType":"","propertyType":"FLAT"}},"cachedAddresses":"[{\"singleLineAddress\":\"2, Example Road, EG\",\"localCouncilId\":2,\"uprn\":2123456,\"buildingNumber\":\"2\",\"postcode\":\"EG\"}]","isAddressAlreadyRegistered":"false"}', 'urn:fdc:gov.uk:2022:UVWXY'),
       (3, current_date-2, current_date-2, '3', '{"journeyData":{"lookup-address":{"houseNameOrNumber":"3","postcode":"NW51tl"},"select-address":{"address":"3, Example Road, EG"},"property-type":{"customPropertyType":"","propertyType":"FLAT"}},"cachedAddresses":"[{\"singleLineAddress\":\"3, Example Road, EG\",\"localCouncilId\":2,\"uprn\":3123456,\"buildingNumber\":\"3\",\"postcode\":\"EG\"}]","isAddressAlreadyRegistered":"false"}', 'urn:fdc:gov.uk:2022:UVWXY'),
       (4, current_date-1, current_date-1, '4', '{"journeyData":{"lookup-address":{"houseNameOrNumber":"4","postcode":"NW51tl"},"select-address":{"address":"4, Example Road, EG"},"property-type":{"customPropertyType":"","propertyType":"FLAT"}},"cachedAddresses":"[{\"singleLineAddress\":\"4, Example Road, EG\",\"localCouncilId\":2,\"uprn\":4123456,\"buildingNumber\":\"4\",\"postcode\":\"EG\"}]","isAddressAlreadyRegistered":"false"}', 'urn:fdc:gov.uk:2022:UVWXY'),
       (5, current_date-5, current_date-5, '5', '{"journeyData":{"lookup-address":{"houseNameOrNumber":"5","postcode":"NW51tl"},"select-address":{"address":"5, Example Road, EG"},"property-type":{"customPropertyType":"","propertyType":"FLAT"}},"cachedAddresses":"[{\"singleLineAddress\":\"5, Example Road, EG\",\"localCouncilId\":2,\"uprn\":5123456,\"buildingNumber\":\"5\",\"postcode\":\"EG\"}]","isAddressAlreadyRegistered":"false"}', 'urn:fdc:gov.uk:2022:UVWXY'),
       (6, current_date-6, current_date-6, '6', '{"journeyData":{"lookup-address":{"houseNameOrNumber":"6","postcode":"NW51tl"},"select-address":{"address":"6, Example Road, EG"},"property-type":{"customPropertyType":"","propertyType":"FLAT"}},"cachedAddresses":"[{\"singleLineAddress\":\"6, Example Road, EG\",\"localCouncilId\":2,\"uprn\":6123456,\"buildingNumber\":\"6\",\"postcode\":\"EG\"}]","isAddressAlreadyRegistered":"false"}', 'urn:fdc:gov.uk:2022:UVWXY'),
       (7, current_date-7, current_date-7, '7', '{"journeyData":{"lookup-address":{"houseNameOrNumber":"7","postcode":"NW51tl"},"select-address":{"address":"7, Example Road, EG"},"property-type":{"customPropertyType":"","propertyType":"FLAT"}},"cachedAddresses":"[{\"singleLineAddress\":\"7, Example Road, EG\",\"localCouncilId\":2,\"uprn\":7123456,\"buildingNumber\":\"7\",\"postcode\":\"EG\"}]","isAddressAlreadyRegistered":"false"}', 'urn:fdc:gov.uk:2022:UVWXY'),
       (8, current_date-8, current_date-8, '8', '{"journeyData":{"lookup-address":{"houseNameOrNumber":"8","postcode":"NW51tl"},"select-address":{"address":"8, Example Road, EG"},"property-type":{"customPropertyType":"","propertyType":"FLAT"}},"cachedAddresses":"[{\"singleLineAddress\":\"8, Example Road, EG\",\"localCouncilId\":2,\"uprn\":8123456,\"buildingNumber\":\"8\",\"postcode\":\"EG\"}]","isAddressAlreadyRegistered":"false"}', 'urn:fdc:gov.uk:2022:UVWXY'),
       (9, current_date-9, current_date-9, '9', '{"journeyData":{"lookup-address":{"houseNameOrNumber":"9","postcode":"NW51tl"},"select-address":{"address":"9, Example Road, EG"},"property-type":{"customPropertyType":"","propertyType":"FLAT"}},"cachedAddresses":"[{\"singleLineAddress\":\"9, Example Road, EG\",\"localCouncilId\":2,\"uprn\":9123456,\"buildingNumber\":\"9\",\"postcode\":\"EG\"}]","isAddressAlreadyRegistered":"false"}', 'urn:fdc:gov.uk:2022:UVWXY'),
       (10, current_date-10, current_date-10, '10', '{"journeyData":{"lookup-address":{"houseNameOrNumber":"10","postcode":"NW51tl"},"select-address":{"address":"10, Example Road, EG"},"property-type":{"customPropertyType":"","propertyType":"FLAT"}},"cachedAddresses":"[{\"singleLineAddress\":\"10, Example Road, EG\",\"localCouncilId\":2,\"uprn\":10123456,\"buildingNumber\":\"10\",\"postcode\":\"EG\"}]","isAddressAlreadyRegistered":"false"}', 'urn:fdc:gov.uk:2022:UVWXY'),
       (11, current_date-11, current_date-11, '11', '{"journeyData":{"lookup-address":{"houseNameOrNumber":"11","postcode":"NW51tl"},"select-address":{"address":"11, Example Road, EG"},"property-type":{"customPropertyType":"","propertyType":"FLAT"}},"cachedAddresses":"[{\"singleLineAddress\":\"11, Example Road, EG\",\"localCouncilId\":2,\"uprn\":11123456,\"buildingNumber\":\"11\",\"postcode\":\"EG\"}]","isAddressAlreadyRegistered":"false"}', 'urn:fdc:gov.uk:2022:UVWXY');

INSERT INTO landlord_incomplete_properties (landlord_id, saved_journey_state_id)
VALUES (1, 1),
       (1, 2),
       (1, 3),
       (1, 4),
       (1, 5),
       (1, 6),
       (1, 7),
       (1, 8),
       (1, 9),
       (1, 10),
       (1, 11);
