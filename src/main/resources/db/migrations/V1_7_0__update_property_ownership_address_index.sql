DROP INDEX property_ownership_single_line_address_idx;

CREATE INDEX property_ownership_single_line_address_gin_idx ON property_ownership USING gin (single_line_address gin_trgm_ops) WHERE is_active;

ALTER TABLE property_ownership ADD is_in_gist_index BOOLEAN NOT NULL GENERATED ALWAYS AS (is_active) STORED;

CREATE INDEX property_ownership_single_line_address_gist_idx ON property_ownership USING gist (single_line_address gist_trgm_ops) WHERE is_in_gist_index;
