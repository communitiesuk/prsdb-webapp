-- Postal addresses for freeholders and superior landlords are no longer stored. They are still collected
-- in the journeys and used to notify the party, but are not persisted. Address rows are never deleted, so
-- any only referenced by these columns are left in place.
ALTER TABLE freeholder DROP COLUMN address_id;
ALTER TABLE superior_landlord DROP COLUMN address_id;
