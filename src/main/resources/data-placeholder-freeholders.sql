-- Seed data is loaded after the V1_62_0 freeholder backfill has already run, so properties that
-- need a "no details known" placeholder freeholder get one here instead. Run after every seed
-- script by IntegrationTestHelper, and listed after the seeds in data-locations, so individual
-- seed files do not have to repeat it. Idempotent, so it is safe to run over already-seeded data.
-- data-test.sql and data-integration.sql keep their own copy: they are picked up by
-- spring.sql.init.platform rather than an explicit data-locations list, so there is nowhere
-- to append this script for those environments without adding one.
INSERT INTO freeholder (property_ownership_id)
SELECT po.id
FROM property_ownership po
WHERE po.ownership_type IN (0, 1, 3, 4, 7, 8)
  AND NOT EXISTS (SELECT 1 FROM freeholder f WHERE f.property_ownership_id = po.id);
