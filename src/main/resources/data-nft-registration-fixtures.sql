WITH owners AS (
    SELECT row_number() OVER (ORDER BY selected.subject) AS allocation, selected.subject
    FROM (
        SELECT l.individual_subject_identifier AS subject
        FROM (
            SELECT *
            FROM landlord
            WHERE individual_subject_identifier LIKE 'urn:fdc:gov.uk:2022:%'
            ORDER BY individual_subject_identifier
            OFFSET 4000 LIMIT 600
        ) l
        WHERE l.is_active AND l.individual_has_accepted_privacy_notice
          AND l.individual_subject_identifier LIKE 'urn:fdc:gov.uk:2022:%'
          AND NOT EXISTS (
              SELECT 1 FROM landlord_incomplete_properties lip
              WHERE lip.user_id = l.individual_subject_identifier
          )
        ORDER BY l.individual_subject_identifier
         LIMIT 456
    ) selected
), addresses AS (
    SELECT row_number() OVER (ORDER BY selected.uprn) AS allocation, selected.*
    FROM (
        SELECT a.uprn, a.single_line_address AS address, a.postcode,
               a.building_number AS building, a.local_council_id AS "localCouncilId"
        FROM address a
        WHERE a.is_active AND a.uprn IS NOT NULL AND a.uprn > 0
          AND a.local_council_id IS NOT NULL AND a.building_number IS NOT NULL
          AND a.building_number <> '' AND a.postcode IS NOT NULL
          AND NOT EXISTS (
              SELECT 1 FROM property_ownership p WHERE p.address_id = a.id AND p.is_active
          )
          AND NOT EXISTS (
              SELECT 1 FROM saved_journey_state s
              WHERE s.serialized_state::jsonb #>> '{journeyData,select-address,address}' = a.single_line_address
          )
        ORDER BY a.uprn
         LIMIT 456 OFFSET 1000
    ) selected
), fixtures AS (
    SELECT owners.subject, addresses.address, addresses.postcode, addresses.building,
           addresses."localCouncilId", addresses.uprn
    FROM owners JOIN addresses USING (allocation)
)
SELECT COALESCE(json_agg(fixtures ORDER BY subject), '[]'::json) FROM fixtures;
