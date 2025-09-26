COMMENT ON TABLE address IS 'dataPackageVersionId=';

ALTER TABLE address
    ALTER COLUMN single_line_address TYPE VARCHAR(1000);

ALTER TABLE address
    ALTER COLUMN sub_building TYPE VARCHAR(500);

ALTER TABLE address
    ALTER COLUMN postcode SET NOT NULL;