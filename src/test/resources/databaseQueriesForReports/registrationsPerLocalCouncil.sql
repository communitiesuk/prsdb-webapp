-- A :reference_date of CURRENT_DATE will count records between just before midnight last night and 14 days before that.
-- A :reference_date of make_date(2025, 09, 15) will count records from 1st September (2025-09-01 00:00:00.000000) - 14th September inclusive (2025-09-015 00:00:00.000000 is NOT included).

-- Landlord (not usually associated with a local council but we can use claimed passcodes)
SELECT
    lc.name AS local_council_name,
    p.local_council_id AS local_council_id,
    COUNT(*) FILTER (WHERE p.last_modified_date < :reference_date) AS total_claimed_passcodes,
    COUNT(*) FILTER (WHERE p.last_modified_date >= :reference_date - INTERVAL '14 DAYS' AND p.last_modified_date < :reference_date) AS newly_claimed_passcodes_last_2_weeks
FROM
    passcode p
    JOIN local_council lc ON p.local_council_id = lc.id
    -- only include passcodes claimed by a currently registered landlord
    JOIN landlord l ON p.subject_identifier = l.subject_identifier
GROUP BY
    lc.name, p.local_council_id
ORDER BY lc.name;

-- Property registrations by local council that the landlord is associated with (via claimed passcode)
-- Note this can be different from the local council the property is in
SELECT
    lc.name AS local_council_name,
    COUNT(*) FILTER (WHERE ownerships.po_created_date < :reference_date) AS total_property_ownerships,
    COUNT(*) FILTER (WHERE ownerships.po_created_date >= :reference_date - INTERVAL '14 DAYS' AND ownerships.po_created_date < :reference_date) AS new_property_ownerships_last_2_weeks
FROM(
        SELECT
            p.local_council_id AS local_council_id,
            po.created_date as po_created_date,
            po.last_modified_date AS po_updated_date
        FROM property_ownership po
                JOIN landlord l ON po.primary_landlord_id = l.id
                JOIN passcode p ON p.subject_identifier = l.subject_identifier
    ) ownerships
        JOIN local_council lc ON ownerships.local_council_id = lc.id
GROUP BY
    lc.name, lc.id
ORDER BY lc.name;

-- Local council user registrations
-- local_council_id might be useful for correlating with analytics (some page urls include the lc id)
SELECT
    lcu.is_manager as is_admin,
    lc.name AS local_council_name,
    lcu.local_council_id AS local_council_id,
    COUNT(*) FILTER (WHERE lcu.created_date < :reference_date )AS total_lc_users,
    COUNT(*) FILTER (WHERE lcu.created_date >= :reference_date - INTERVAL '14 DAYS' AND lcu.created_date < :reference_date) AS new_lc_users_last_2_weeks
FROM
    local_council_user lcu
        JOIN local_council lc ON lcu.local_council_id = lc.id
    -- exclude Bath as this is test data
    WHERE lcu.local_council_id != 2
GROUP BY
    lc.name, lcu.local_council_id, lcu.is_manager
ORDER BY lc.name;

-- Passcodes generated
SELECT
    lc.name AS local_council_name,
    COUNT(*) FILTER (WHERE p.created_date < :reference_date) AS total_passcodes_generated,
    COUNT(*) FILTER (WHERE p.created_date >= :reference_date - INTERVAL '14 DAYS' AND p.created_date < :reference_date) AS new_passcodes_last_2_weeks
FROM
    passcode p
        JOIN local_council lc ON p.local_council_id = lc.id
    -- exclude Bath as this is test data
    WHERE lc.id != 2
GROUP BY
    lc.name, p.local_council_id
ORDER BY lc.name;
