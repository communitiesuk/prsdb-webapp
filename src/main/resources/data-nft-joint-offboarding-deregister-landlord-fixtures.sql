WITH eligible AS (
    SELECT l.individual_subject_identifier AS subject
    FROM landlord l
    WHERE l.individual_is_active
      AND l.individual_has_accepted_privacy_notice
      AND l.individual_subject_identifier LIKE 'urn:fdc:gov.uk:2022:%'
      AND NOT EXISTS (
          SELECT 1
          FROM ownership_link ol
          WHERE ol.landlord_id = l.id
      )
      AND NOT EXISTS (
          SELECT 1
          FROM landlord_incomplete_properties incomplete
          WHERE incomplete.user_id = l.individual_subject_identifier
      )
    ORDER BY l.individual_subject_identifier
    LIMIT 101
)
SELECT COALESCE(
    json_agg(
        json_build_object(
            'action', 'deregister-landlord',
            'subject', subject
        )
        ORDER BY subject
    ),
    '[]'::json
)
FROM eligible;
