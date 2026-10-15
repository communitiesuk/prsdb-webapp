-- Overlay script: makes the signed-in mock user (UVWXY) an organisational landlord user of organisation 100. Use it in
-- any test that needs the signed-in user to belong to an organisational landlord. Note that organisation 100 is not the
-- organisation that issues the test invitations (36). Apply on top of a base seed script, and on top of the not a
-- landlord overlay if the org landlord user row should be the only thing giving the signed-in user a landlord role, e.g.
--   listOf("data-local.sql", "data-overlay-mockuser-is-not-a-landlord.sql", "data-overlay-mockuser-is-org-landlord-user.sql")
INSERT INTO organisational_landlord_user (organisation_landlord_id, subject_identifier, name, email, role, created_date)
VALUES (100, 'urn:fdc:gov.uk:2022:UVWXY', 'Mock User', 'mock.user@example.com', 1, '07/23/26');
