-- Gives the mock user an incomplete property.
-- Apply on top of data-mockuser-inactive-landlord.sql.

INSERT INTO saved_journey_state (id, created_date, last_modified_date, journey_id, serialized_state, subject_identifier)
VALUES (1, current_date, current_date, 'example-incomplete-journey1', '{}', 'urn:fdc:gov.uk:2022:UVWXY');

INSERT INTO landlord_incomplete_properties (user_id, saved_journey_state_id)
VALUES ('urn:fdc:gov.uk:2022:UVWXY', 1);
