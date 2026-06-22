ALTER TABLE property_ownership
    ADD COLUMN last_modified_by_landlord_id BIGINT;

-- Backfill to the property's current first/primary landlord (the lowest ownership_link id),
-- matching the previous display behaviour so that a NULL can only ever mean the landlord was deleted.
UPDATE property_ownership po
SET last_modified_by_landlord_id = ol.landlord_id
FROM (
    SELECT DISTINCT ON (landlordship_id) landlordship_id, landlord_id
    FROM ownership_link
    ORDER BY landlordship_id, id
) ol
WHERE ol.landlordship_id = po.id;

ALTER TABLE property_ownership
    ADD CONSTRAINT fk_property_ownership_on_last_modified_by_landlord
        FOREIGN KEY (last_modified_by_landlord_id) REFERENCES landlord (id)
        ON DELETE SET NULL;
