ALTER TABLE landlord_access_link RENAME TO organisation_users;

ALTER TABLE organisation_users RENAME COLUMN landlord_id TO organisation_landlord_id;

-- Rename indexes to match new table and column names
ALTER INDEX idx_landlord_access_link_subject RENAME TO idx_organisation_users_subject;
ALTER INDEX idx_landlord_access_link_landlord RENAME TO idx_organisation_users_organisation_landlord;

-- Rename the unique constraint
ALTER TABLE organisation_users RENAME CONSTRAINT landlord_access_link_landlord_id_subject_identifier_key
    TO organisation_users_organisation_landlord_id_subject_identifier_key;
