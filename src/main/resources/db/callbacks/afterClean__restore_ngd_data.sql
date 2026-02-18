-- Restore tables to state expected in migration V1.1
ALTER TABLE temp.address RENAME COLUMN local_council_id TO local_authority_id;
ALTER TABLE temp.local_council RENAME TO local_authority;

ALTER TABLE temp.address SET SCHEMA public;
ALTER TABLE temp.local_authority SET SCHEMA public;
DROP SCHEMA temp;
