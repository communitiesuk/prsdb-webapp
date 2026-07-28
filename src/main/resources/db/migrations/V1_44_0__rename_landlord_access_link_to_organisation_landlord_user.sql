ALTER TABLE landlord_access_link RENAME TO organisation_landlord_user;

ALTER TABLE organisation_landlord_user RENAME COLUMN landlord_id TO organisation_landlord_id;

-- Rename indexes to match new table and column names
ALTER INDEX idx_landlord_access_link_subject RENAME TO idx_organisation_landlord_user_subject;
ALTER INDEX idx_landlord_access_link_landlord RENAME TO idx_organisation_landlord_user_organisation_landlord;

-- Rename the unique constraint, organisation -> org in new name else it'd be truncated
ALTER TABLE organisation_landlord_user RENAME CONSTRAINT landlord_access_link_landlord_id_subject_identifier_key
    TO org_landlord_user_org_landlord_id_subject_identifier_key;
