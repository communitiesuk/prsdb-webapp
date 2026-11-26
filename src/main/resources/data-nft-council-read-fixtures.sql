-- Read-only. Even council administrators are not operator removal targets.
-- Limit property sampling to an indexed 10,000-ID window, not the nationwide register.
WITH users AS MATERIALIZED (
    SELECT DISTINCT ON (u.local_council_id)
           u.subject_identifier AS subject, u.name AS "actorName",
           u.local_council_id AS "councilId", c.name AS "councilName",
           target.id AS "targetId", target.name AS "targetName", target.email AS "targetEmail",
           target.subject_identifier AS "targetSubject", target.is_manager AS "targetManager"
    FROM local_council_user u
    JOIN local_council c ON c.id = u.local_council_id
    JOIN local_council_user target ON target.local_council_id = u.local_council_id
        AND target.id <> u.id AND target.id % 2 = 1
    WHERE u.id > 100 AND u.id % 2 = 0 AND u.is_manager
      AND u.subject_identifier LIKE 'urn:fdc:gov.uk:2022:%'
      AND u.email LIKE '%@example.com'
      AND target.email LIKE '%@example.com'
      AND NOT EXISTS (
          SELECT 1 FROM system_operator o WHERE o.subject_identifier = u.subject_identifier
      )
      AND NOT EXISTS (
          SELECT 1 FROM landlord l WHERE l.individual_subject_identifier = u.subject_identifier
      )
      AND NOT EXISTS (
          SELECT 1 FROM organisational_landlord_user o WHERE o.subject_identifier = u.subject_identifier
      )
    ORDER BY u.local_council_id, u.id, target.id
    LIMIT 101
), properties AS MATERIALIZED (
    SELECT p.id AS "propertyId", a.single_line_address AS address, a.uprn::text AS uprn,
           a.local_council_id AS "councilId", pr.number AS "propertyRegistrationNumber",
           l.id AS "landlordId", l.individual_name AS "landlordName",
           lr.number AS "landlordRegistrationNumber", licence.license_type AS "licenseType",
           l.individual_subject_identifier AS "landlordSubject"
    FROM property_ownership p
    JOIN address a ON a.id = p.address_id
    JOIN ownership_link ol ON ol.landlordship_id = p.id
    JOIN landlord l ON l.id = ol.landlord_id
    JOIN registration_number pr ON pr.id = p.registration_number_id
    JOIN registration_number lr ON lr.id = l.registration_number_id
    LEFT JOIN license licence ON licence.id = p.license_id
    WHERE p.id BETWEEN 100027 AND 110026 AND p.is_active
      AND l.is_active AND l.individual_has_accepted_privacy_notice
      AND l.individual_subject_identifier LIKE 'urn:fdc:gov.uk:2022:%'
      AND a.uprn IS NOT NULL AND a.is_active
), fixtures AS (
    SELECT users.*, chosen."propertyId", chosen.address, chosen.uprn,
           chosen."propertyRegistrationNumber", chosen."landlordId",
           chosen."landlordName", chosen."landlordRegistrationNumber", chosen."licenseType",
           chosen."landlordSubject"
    FROM users
    CROSS JOIN LATERAL (
        SELECT * FROM properties
        WHERE properties."councilId" = users."councilId"
        ORDER BY "propertyId", "landlordId"
        LIMIT 1
    ) chosen
)
SELECT COALESCE(json_agg(fixtures ORDER BY "councilId"), '[]'::json) FROM fixtures;
