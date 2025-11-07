ALTER TABLE property_ownership ADD property_build_type SMALLINT;

UPDATE property_ownership po SET property_build_type = (
    SELECT p.property_build_type FROM property p WHERE p.id = po.property_id
);

ALTER TABLE property_ownership ALTER COLUMN property_build_type SET NOT NULL;

ALTER TABLE property_ownership ADD address_id BIGINT REFERENCES address (id);

UPDATE property_ownership po SET address_id = (
    SELECT p.address_id FROM property p WHERE p.id = po.property_id
);

ALTER TABLE property_ownership ALTER COLUMN address_id SET NOT NULL;

CREATE UNIQUE INDEX property_ownership_active_address_id_key ON property_ownership USING btree (address_id) WHERE is_active;

ALTER TABLE property_ownership DROP COLUMN property_id;

DROP TABLE property;
