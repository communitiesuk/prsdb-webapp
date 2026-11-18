-- Read-only. These owners are outside the existing invitation/update pools.
-- Manual-address allocations do not consume or impersonate real OS addresses.
WITH owners AS MATERIALIZED (
    SELECT id, individual_subject_identifier AS subject, is_active, individual_has_accepted_privacy_notice
    FROM landlord
    WHERE individual_subject_identifier LIKE 'urn:fdc:gov.uk:2022:%'
    ORDER BY individual_subject_identifier
    OFFSET 3000 LIMIT 1000
), eligible AS (
    SELECT subject, row_number() OVER (ORDER BY subject) AS ordinal
    FROM owners
    WHERE is_active AND individual_has_accepted_privacy_notice
      AND NOT EXISTS (
          SELECT 1 FROM landlord_incomplete_properties lip WHERE lip.user_id = owners.subject
      )
      AND NOT EXISTS (
          SELECT 1 FROM system_operator o WHERE o.subject_identifier = owners.subject
      )
      AND NOT EXISTS (
          SELECT 1 FROM local_council_user u WHERE u.subject_identifier = owners.subject
      )
      AND NOT EXISTS (
          SELECT 1 FROM organisational_landlord_user u WHERE u.subject_identifier = owners.subject
      )
    ORDER BY subject
    LIMIT 456
), allocations AS (
    SELECT subject, 9200000000 + ordinal AS uprn, ordinal::text AS building,
           'FA1 1AA'::text AS postcode, 1::bigint AS "localCouncilId"
    FROM eligible
), fixtures AS (
    SELECT *, 'NFT registration ' || uprn || ', Performance Town, ' || postcode AS address
    FROM allocations
    WHERE EXISTS (SELECT 1 FROM local_council c WHERE c.id = "localCouncilId")
      AND NOT EXISTS (SELECT 1 FROM address a WHERE a.uprn = allocations.uprn)
      AND NOT EXISTS (
          SELECT 1 FROM address a
          WHERE a.single_line_address = 'NFT registration ' || allocations.uprn || ', Performance Town, ' || allocations.postcode
      )
)
SELECT COALESCE(json_agg(fixtures ORDER BY subject), '[]'::json) FROM fixtures;
