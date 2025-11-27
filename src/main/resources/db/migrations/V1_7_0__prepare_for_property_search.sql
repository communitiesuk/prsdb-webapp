ALTER TABLE property_ownership
    ADD local_council_id INTEGER,
    ADD is_in_gist_index BOOLEAN NOT NULL GENERATED ALWAYS AS (is_active) STORED;

DROP PROCEDURE update_property_ownership_single_line_addresses();

CREATE PROCEDURE update_property_ownership_addresses()
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE property_ownership po SET (single_line_address, local_council_id) = (
        SELECT a.single_line_address, a.local_council_id
        FROM address a
        WHERE a.id = po.address_id
    );
END;
$$;

CALL update_property_ownership_addresses();

DROP TRIGGER insert_property_ownership_single_line_address ON property_ownership;

DROP FUNCTION insert_property_ownership_single_line_address();

CREATE FUNCTION insert_property_ownership_address()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    propertyOwnershipId BIGINT;
BEGIN
    propertyOwnershipId := NEW.id;
    UPDATE property_ownership po SET (single_line_address, local_council_id) = (
        SELECT a.single_line_address, a.local_council_id
        FROM address a
        WHERE a.id = po.address_id
    )
    WHERE po.id = propertyOwnershipId;
    RETURN NULL;
END;
$$;

CREATE TRIGGER insert_property_ownership_address
AFTER INSERT ON property_ownership
FOR EACH ROW
EXECUTE FUNCTION insert_property_ownership_address();

DROP INDEX property_ownership_single_line_address_idx;

CREATE INDEX property_ownership_single_line_address_gin_idx ON property_ownership USING gin (single_line_address gin_trgm_ops) WHERE is_active;

CREATE INDEX property_ownership_single_line_address_gist_idx ON property_ownership USING gist (single_line_address gist_trgm_ops) WHERE is_in_gist_index;

CREATE INDEX ON property_ownership USING hash (local_council_id) WHERE is_active;
