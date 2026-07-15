ALTER TABLE property_ownership ADD COLUMN is_occupied BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE property_ownership
SET is_occupied = current_num_tenants > 0;
