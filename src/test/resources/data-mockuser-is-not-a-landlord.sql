-- Overlay script: reassigns individual landlord 1 away from the signed-in mock user (UVWXY) to a new user, so that
-- the signed-in user has no landlord role.
-- Apply on top of a base seed script, e.g.
--   listOf("data-local.sql", "data-mockuser-is-not-a-landlord.sql")
INSERT INTO prsdb_user (id, created_date)
VALUES ('urn:fdc:gov.uk:2022:NOTLL', '10/14/24');

UPDATE landlord
SET individual_subject_identifier = 'urn:fdc:gov.uk:2022:NOTLL'
WHERE id = 1;
