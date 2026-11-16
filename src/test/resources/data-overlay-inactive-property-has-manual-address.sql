-- Overlay script: removes the UPRN from the address of inactive property ownership 52, as if the landlord had entered
-- the address manually, so there is no UPRN to look up an EPC with. Use it in tests of the property reactivation
-- journey's EPC task. Apply on top of data-local.sql, e.g.
--   listOf("data-local.sql", "data-overlay-inactive-property-has-manual-address.sql")
UPDATE address
SET uprn = NULL
WHERE id = 60;
