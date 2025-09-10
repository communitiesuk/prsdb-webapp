-- Landlord (not usually associated with a local council but we can use claimed passcodes)

-- Property registrations
SELECT
    la.name AS local_council_name,
    la.id AS local_council_id,
    COUNT(*) AS total_property_ownerships,
    COUNT(*) FILTER (WHERE ownerships.po_created_date >= NOW() - INTERVAL '14 DAYS') AS new_property_ownerships_last_2_weeks,
    COUNT(*) FILTER (WHERE ownerships.po_updated_date >= NOW() - INTERVAL '14 DAYS') AS updated_property_ownerships_last_2_weeks
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
    la.name, la.id;


-- Local council user registrations
-- local_council_id might be useful for correlating with analytics (some page urls include the lc id)
SELECT
    la.name AS local_council_name,
    lau.local_authority_id AS local_council_id,
    COUNT(*) AS total_lc_users,
    COUNT(*) FILTER (WHERE lau.created_date >= NOW() - INTERVAL '14 DAYS') AS new_lc_users_last_2_weeks,
    COUNT(*) FILTER (WHERE lau.last_modified_date >= NOW() - INTERVAL '14 DAYS') AS updated_lc_users_last_2_weeks
FROM
    local_authority_user lau
        JOIN local_authority la ON lau.local_authority_id = la.id
GROUP BY
    la.name, lau.local_authority_id;