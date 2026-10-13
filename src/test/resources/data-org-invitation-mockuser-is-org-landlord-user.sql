-- Overlay script: makes the signed-in mock user (UVWXY) an organisational landlord user of organisation 100, which
-- is NOT the organisation that issues the test invitations (36). Apply on top of the non-landlord overlay so that
-- the org landlord user row is the only thing giving the signed-in user a landlord role, e.g.
--   listOf("data-local.sql", "data-mockuser-is-not-a-landlord.sql", "data-mockuser-is-org-landlord-user.sql")
INSERT INTO organisational_landlord_user (organisation_landlord_id, subject_identifier, name, email, role, created_date)
VALUES (100, 'urn:fdc:gov.uk:2022:UVWXY', 'Mock User', 'mock.user@example.com', 1, '07/23/26');
