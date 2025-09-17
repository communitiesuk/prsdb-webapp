-- A :reference_date of CURRENT_DATE will count records between just before midnight last night and 14 days before that.
-- A :reference_date of make_date(2025, 09, 15) will count records from 1st September (2025-09-01 00:00:00.000000) - 14th September inclusive (2025-09-015 00:00:00.000000 is NOT included).

-- Landlord (not usually associated with a local council but we can use claimed passcodes)
SELECT
    la.name AS local_council_name,
    p.local_authority_id AS local_council_id,
    COUNT(*) AS total_claimed_passcodes,
    COUNT(*) FILTER (WHERE p.last_modified_date >= :reference_date - INTERVAL '14 DAYS' AND p.last_modified_date < :reference_date) AS newly_claimed_passcodes_last_2_weeks
FROM
    passcode p
    JOIN local_authority la ON p.local_authority_id = la.id
    -- only include passcodes claimed by a currently registered landlord
    JOIN landlord l ON p.subject_identifier = l.subject_identifier
GROUP BY
    la.name, p.local_authority_id
ORDER BY la.name;

-- Property registrations
SELECT
    la.name AS local_council_name,
    COUNT(*) AS total_property_ownerships,
    COUNT(*) FILTER (WHERE ownerships.po_created_date >= :reference_date - INTERVAL '14 DAYS' AND ownerships.po_created_date < :reference_date) AS new_property_ownerships_last_2_weeks
FROM(
        SELECT
            a.local_authority_id AS local_council_id,
            po.created_date as po_created_date,
            po.last_modified_date AS po_updated_date
        FROM property p
                 JOIN address a ON p.address_id = a.id
                 JOIN property_ownership po ON p.id = po.property_id
    ) ownerships
        JOIN local_authority la ON ownerships.local_council_id = la.id
GROUP BY
    la.name, la.id
ORDER BY la.name;

-- Local council user registrations
-- local_council_id might be useful for correlating with analytics (some page urls include the lc id)
SELECT
    lau.is_manager as is_admin,
    la.name AS local_council_name,
    lau.local_authority_id AS local_council_id,
    COUNT(*) AS total_lc_users,
    COUNT(*) FILTER (WHERE lau.created_date >= :reference_date - INTERVAL '14 DAYS' AND lau.created_date < :reference_date) AS new_lc_users_last_2_weeks
FROM
    local_authority_user lau
        JOIN local_authority la ON lau.local_authority_id = la.id
    -- exclude Bath as this is test data
    WHERE lau.local_authority_id != 2
GROUP BY
    la.name, lau.local_authority_id, lau.is_manager
ORDER BY la.name;