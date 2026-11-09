-- Landlord active status now applies to all landlord types, not just individuals
ALTER TABLE landlord RENAME COLUMN individual_is_active TO is_active;
UPDATE landlord SET is_active = true WHERE is_active IS NULL;
ALTER TABLE landlord ALTER COLUMN is_active SET NOT NULL;
ALTER TABLE landlord ALTER COLUMN is_active SET DEFAULT true;

-- Whether the landlord has ever registered or joined a property
ALTER TABLE landlord ADD COLUMN has_registered_property BOOLEAN NOT NULL DEFAULT false;
UPDATE landlord l
SET has_registered_property = true
WHERE EXISTS (SELECT 1 FROM ownership_link ol WHERE ol.landlord_id = l.id);

ALTER TABLE landlord ADD COLUMN deactivation_date DATE;

ALTER TABLE property_ownership ADD COLUMN deactivation_date DATE;
