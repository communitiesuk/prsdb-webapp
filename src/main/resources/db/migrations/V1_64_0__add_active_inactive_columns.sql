-- Landlord active status now applies to all landlord types, not just individuals.
-- individual_is_active is kept, with a default, so the previous app version keeps working while this release is deployed.
-- It will be dropped in a later release once no running app version uses it.
ALTER TABLE landlord ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT true;
UPDATE landlord SET is_active = individual_is_active WHERE individual_is_active IS NOT NULL;
ALTER TABLE landlord ALTER COLUMN individual_is_active SET DEFAULT true;

-- Whether the landlord has ever registered or joined a property
ALTER TABLE landlord ADD COLUMN has_registered_property BOOLEAN NOT NULL DEFAULT false;
UPDATE landlord l
SET has_registered_property = true
WHERE EXISTS (SELECT 1 FROM ownership_link ol WHERE ol.landlord_id = l.id);

ALTER TABLE landlord ADD COLUMN deactivation_date DATE;

ALTER TABLE property_ownership ADD COLUMN deactivation_date DATE;
