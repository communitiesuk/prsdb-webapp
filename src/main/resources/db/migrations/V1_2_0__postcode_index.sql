CREATE INDEX IF NOT EXISTS address_postcode_idx ON address (replace(postcode, ' ', '') varchar_pattern_ops) WHERE is_active AND uprn IS NOT NULL;
