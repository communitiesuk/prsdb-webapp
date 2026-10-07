-- Overlay script: demotes the signed-in org landlord user (ORG01) from admin to editor.
-- Apply on top of an org landlord seed script, e.g.
--   listOf("data-mockuser-org-landlord-trust.sql", "data-org-landlord-user-is-editor.sql")
UPDATE organisational_landlord_user
SET role = 1
WHERE subject_identifier = 'urn:fdc:gov.uk:2022:ORG01';
