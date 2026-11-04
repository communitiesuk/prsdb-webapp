-- Address lookup data for the Gatling registration journey in NFT (PostcodeLookup for EG1 2AA, house number 1).
-- The high IDs are excluded by NftDataSeederDao so these rows stay unallocated; lower-ID local fixtures use the same labels.
-- They use reserved ids and uprns far above the range the AddressBase/NGD loader allocates from.

DO $$
DECLARE
    fixture_subject TEXT := 'urn:fdc:gov.uk:2022:mGHDySEVfCsvfvc6lVWf6Qt9Dv0ZxPQWKoEzcjnBlUo';
    candidate_landlord_id BIGINT;
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM landlord WHERE individual_subject_identifier = fixture_subject
    ) THEN
        SELECT id
        INTO candidate_landlord_id
        FROM landlord l
        WHERE l.landlord_type = 0
          AND l.individual_is_active
          AND l.individual_has_accepted_privacy_notice
          AND l.individual_subject_identifier IS NOT NULL
          AND NOT EXISTS (SELECT 1 FROM ownership_link ol WHERE ol.landlord_id = l.id)
        ORDER BY l.id
        LIMIT 1;

        IF candidate_landlord_id IS NULL THEN
            RAISE EXCEPTION 'Cannot find an unlinked active individual landlord for the Gatling fixture subject';
        END IF;

        INSERT INTO prsdb_user (id, created_date)
        VALUES (fixture_subject, current_timestamp)
        ON CONFLICT (id) DO NOTHING;

        UPDATE landlord
        SET individual_subject_identifier = fixture_subject
        WHERE id = candidate_landlord_id;
    END IF;
END $$;

INSERT INTO address (id, created_date, uprn, single_line_address, postcode, building_number, street_name, local_council_id)
VALUES (9100000001, current_timestamp, 9100000001, '1 PRSDB Square, EG1 2AA', 'EG1 2AA', '1', 'PRSDB Square', 1),
       (9100000002, current_timestamp, 9100000002, '2 PRSDB Square, EG1 2AA', 'EG1 2AA', '2', 'PRSDB Square', 1),
       (9100000003, current_timestamp, 9100000003, '3 PRSDB Square, EG1 2AA', 'EG1 2AA', '3', 'PRSDB Square', 1),
       (9100000004, current_timestamp, 9100000004, '4 PRSDB Square, EG1 2AA', 'EG1 2AA', '4', 'PRSDB Square', 1),
       (9100000005, current_timestamp, 9100000005, '5 PRSDB Square, EG1 2AA', 'EG1 2AA', '5', 'PRSDB Square', 1)
ON CONFLICT DO NOTHING;
