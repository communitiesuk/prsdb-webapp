-- Clear NgdAddressLoader progress so the next run loads data package 25924 from its initial full supply,
-- backfilling country and GSS code on existing addresses
COMMENT ON TABLE address IS 'dataPackageVersionId=';
