INSERT INTO one_login_user (id, created_date)
VALUES ('urn:fdc:gov.uk:2022:UVWXY', '10/14/24');

INSERT INTO registration_number (id, created_date, number, type)
VALUES (1, '09/13/24', 2001001001, 1);

INSERT INTO address (id, created_date, last_modified_date, uprn, single_line_address, local_authority_id)
VALUES  (1, '09/13/24', '09/13/24', 1, '1 Fictional Road', 2);

INSERT INTO landlord (id, created_date, last_modified_date, registration_number_id, address_id, date_of_birth,
                      is_active, phone_number, subject_identifier, name, email, country_of_residence, is_verified, has_accepted_privacy_notice)
VALUES (1, '09/13/24', '09/13/24', 1, 1, '09/13/2000', true, 07111111111, 'urn:fdc:gov.uk:2022:UVWXY',
        'Alexander Smith', 'alex.surname@example.com', 'England or Wales', false, true);
SELECT setval(pg_get_serial_sequence('landlord', 'id'), (SELECT MAX(id) FROM landlord));

INSERT INTO form_context (id, created_date, last_modified_date, journey_type, context, subject_identifier)
VALUES (1, current_date, current_date,3, '{"lookup-address":{"houseNameOrNumber":"6","postcode":"NW5 1tl"},"looked-up-addresses":"[{\"singleLineAddress\":\"1, Example Road, EG\",\"localAuthorityId\":2,\"uprn\":1123456,\"buildingNumber\":\"1\",\"postcode\":\"EG\"},{\"singleLineAddress\":\"2, Example Road, EG\",\"localAuthorityId\":2,\"uprn\":2123456,\"buildingNumber\":\"2\",\"postcode\":\"EG\"},{\"singleLineAddress\":\"3, Example Road, EG\",\"localAuthorityId\":2,\"uprn\":3123456,\"buildingNumber\":\"3\",\"postcode\":\"EG\"},{\"singleLineAddress\":\"4, Example Road, EG\",\"localAuthorityId\":4,\"uprn\":4123456,\"buildingNumber\":\"4\",\"postcode\":\"EG\"},{\"singleLineAddress\":\"5, Example Road, EG\",\"localAuthorityId\":5,\"uprn\":5123456,\"buildingNumber\":\"5\",\"postcode\":\"EG\"}]","select-address":{"address":"4, Example Road, EG"},"property-type":{"customPropertyType":"","propertyType":"FLAT"}}','urn:fdc:gov.uk:2022:UVWXY'),
       (2, current_date, current_date,3, '{"lookup-address":{"houseNameOrNumber":"6","postcode":"NW5 1tl"},"looked-up-addresses":"[{\"singleLineAddress\":\"1, Example Road, EG\",\"localAuthorityId\":2,\"uprn\":1123456,\"buildingNumber\":\"1\",\"postcode\":\"EG\"},{\"singleLineAddress\":\"2, Example Road, EG\",\"localAuthorityId\":2,\"uprn\":2123456,\"buildingNumber\":\"2\",\"postcode\":\"EG\"},{\"singleLineAddress\":\"3, Example Road, EG\",\"localAuthorityId\":2,\"uprn\":3123456,\"buildingNumber\":\"3\",\"postcode\":\"EG\"},{\"singleLineAddress\":\"4, Example Road, EG\",\"localAuthorityId\":4,\"uprn\":4123456,\"buildingNumber\":\"4\",\"postcode\":\"EG\"},{\"singleLineAddress\":\"5, Example Road, EG\",\"localAuthorityId\":5,\"uprn\":5123456,\"buildingNumber\":\"5\",\"postcode\":\"EG\"}]","select-address":{"address":"5, Example Road, EG"},"property-type":{"customPropertyType":"","propertyType":"FLAT"}}','urn:fdc:gov.uk:2022:UVWXY');