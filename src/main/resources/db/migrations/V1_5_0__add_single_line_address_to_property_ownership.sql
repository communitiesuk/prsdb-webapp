ALTER TABLE property_ownership ADD single_line_address VARCHAR(1000) DEFAULT '' NOT NULL;

CREATE PROCEDURE update_property_ownership_single_line_addresses()
    LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE property_ownership po SET single_line_address = (
        SELECT a.single_line_address
        FROM address a
        WHERE a.id = po.address_id
    );
END;
$$;

CALL update_property_ownership_single_line_addresses();

CREATE FUNCTION insert_property_ownership_single_line_address()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS $$
DECLARE
    propertyOwnershipId BIGINT;
BEGIN
    propertyOwnershipId := NEW.id;
    UPDATE property_ownership po SET single_line_address = (
        SELECT a.single_line_address
        FROM address a
        WHERE a.id = po.address_id
    )
    WHERE po.id = propertyOwnershipId;
    RETURN NULL;
END;
$$;

CREATE TRIGGER insert_property_ownership_single_line_address
AFTER INSERT ON property_ownership
FOR EACH ROW
EXECUTE FUNCTION insert_property_ownership_single_line_address();

CREATE INDEX property_ownership_single_line_address_idx ON property_ownership USING gist (single_line_address gist_trgm_ops(siglen=2024)) WHERE is_active;
