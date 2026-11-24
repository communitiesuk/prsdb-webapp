-- Seed data is loaded after the V1_72_0 anniversary backfill has already run, so every landlord linked to a
-- property gets an anniversary here instead: the date they were first linked to one. Run after every seed
-- script by IntegrationTestHelper, and listed after the seeds in data-locations, so individual seed files do
-- not have to repeat it. Idempotent, so it is safe to run over already-seeded data.
-- data-test.sql and data-integration.sql keep their own copy: they are picked up by
-- spring.sql.init.platform rather than an explicit data-locations list, so there is nowhere
-- to append this script for those environments without adding one.
UPDATE landlord l
SET anniversary_day = EXTRACT(DAY FROM sub.first_link_date)::int,
    anniversary_month = EXTRACT(MONTH FROM sub.first_link_date)::int
FROM (
    SELECT ol.landlord_id,
           MIN((ol.created_date AT TIME ZONE 'Europe/London')::date) AS first_link_date
    FROM ownership_link ol
    GROUP BY ol.landlord_id
) sub
WHERE l.id = sub.landlord_id
  AND l.anniversary_day IS NULL;
