-- Read-only. Synthetic seeded identities only; each administrator and mutation
-- target is exclusive to one fixture. Odd manager IDs are reserved for operators.
WITH read_owners AS MATERIALIZED (
    SELECT id FROM landlord
    WHERE individual_subject_identifier LIKE 'urn:fdc:gov.uk:2022:%'
    ORDER BY individual_subject_identifier OFFSET 5000 LIMIT 1000
), actors AS (
    SELECT DISTINCT ON (a.local_council_id)
           a.subject_identifier AS subject, a.id, a.name, a.local_council_id
    FROM local_council_user a
    WHERE a.id > 100 AND a.is_manager AND a.id % 2 = 0
      AND a.has_accepted_privacy_notice AND a.email LIKE '%@example.com'
      AND NOT EXISTS (SELECT 1 FROM landlord l WHERE l.individual_subject_identifier = a.subject_identifier)
      AND NOT EXISTS (SELECT 1 FROM system_operator o WHERE o.subject_identifier = a.subject_identifier)
      AND NOT EXISTS (SELECT 1 FROM organisational_landlord_user o WHERE o.subject_identifier = a.subject_identifier)
      AND EXISTS (
          SELECT 1 FROM local_council_user t
          WHERE t.local_council_id = a.local_council_id
            AND NOT t.is_manager AND t.id > 100 AND t.id % 2 = 0
            AND t.email LIKE '%@example.com'
      )
    ORDER BY a.local_council_id, a.id
    LIMIT 101
), fixtures AS (
    SELECT a.subject, a.name AS "actorName", c.id AS "councilId", c.name AS "councilName",
           t.id AS "targetId", t.name AS "targetName", t.email AS "targetEmail",
           t.subject_identifier AS "targetSubject", t.is_manager AS "targetManager",
           p.id AS "propertyId", p.single_line_address AS address,
           ad.uprn::text AS uprn, pr.number AS "propertyRegistrationNumber",
           l.id AS "landlordId", l.individual_name AS "landlordName",
           lr.number AS "landlordRegistrationNumber", li.license_type AS "licenseType"
    FROM actors a
    JOIN local_council c ON c.id = a.local_council_id
    JOIN LATERAL (
        SELECT u.* FROM local_council_user u
        WHERE u.local_council_id = a.local_council_id AND NOT u.is_manager
          AND u.id > 100 AND u.id % 2 = 0 AND u.email LIKE '%@example.com'
        ORDER BY u.id LIMIT 1
    ) t ON true
    JOIN LATERAL (
        SELECT p.* FROM property_ownership p
        JOIN property_compliance pc ON pc.property_ownership_id = p.id
        WHERE p.local_council_id = c.id AND p.is_active AND p.id > 26
          AND p.license_provide_later IS NOT TRUE
          AND EXISTS (
              SELECT 1 FROM ownership_link ol JOIN landlord l ON l.id = ol.landlord_id
              WHERE ol.landlordship_id = p.id AND l.landlord_type = 0
                AND l.id IN (SELECT id FROM read_owners)
                AND l.individual_is_active AND l.individual_email LIKE '%@example.com'
          )
          AND NOT EXISTS (
             SELECT 1 FROM ownership_link ol
             WHERE ol.landlordship_id = p.id AND ol.landlord_id NOT IN (SELECT id FROM read_owners)
          )
        ORDER BY p.id LIMIT 1
    ) p ON true
    JOIN address ad ON ad.id = p.address_id AND ad.uprn IS NOT NULL
    JOIN registration_number pr ON pr.id = p.registration_number_id
    LEFT JOIN license li ON li.id = p.license_id
    JOIN LATERAL (
        SELECT l.* FROM ownership_link ol JOIN landlord l ON l.id = ol.landlord_id
        WHERE ol.landlordship_id = p.id AND l.landlord_type = 0
          AND l.individual_is_active AND l.individual_email LIKE '%@example.com'
        ORDER BY l.id LIMIT 1
    ) l ON true
    JOIN registration_number lr ON lr.id = l.registration_number_id
), fixture_files AS MATERIALIZED (
    SELECT pc.property_ownership_id, f.object_key AS "objectKey", f.extension
    FROM fixtures
    JOIN property_compliance pc ON pc.property_ownership_id = fixtures."propertyId"
    JOIN gas_safety_uploads g ON g.property_compliance_id = pc.id
    JOIN file_upload f ON f.id = g.gas_safety_file_uploads_id
    UNION
    SELECT pc.property_ownership_id, f.object_key AS "objectKey", f.extension
    FROM fixtures
    JOIN property_compliance pc ON pc.property_ownership_id = fixtures."propertyId"
    JOIN electrical_safety_uploads e ON e.property_compliance_id = pc.id
    JOIN file_upload f ON f.id = e.electrical_safety_file_uploads_id
), with_files AS (
    SELECT fixtures.*, "propertyId" AS "propertyOwnershipId",
           (
               SELECT COALESCE(json_agg(objects ORDER BY "objectKey"), '[]'::json)
               FROM (
                   SELECT "objectKey", extension FROM fixture_files
                   WHERE property_ownership_id = fixtures."propertyId"
               ) objects
           ) AS files
    FROM fixtures
)
SELECT COALESCE(json_agg(with_files ORDER BY "councilId"), '[]'::json) FROM with_files;
