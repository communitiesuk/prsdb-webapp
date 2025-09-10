-- Total current registrations, and those in the last 2 weeks
-- Landlord registrations / updates
SELECT
    COUNT(*) AS total_landlords,
    COUNT(*) FILTER (WHERE created_date >= NOW() - INTERVAL '14 DAYS') AS new_landlords_last_2_weeks,
    COUNT(*) FILTER (WHERE last_modified_date >= NOW() - INTERVAL '14 DAYS') AS updated_landlords_last_2_weeks
FROM
    landlord;

-- Property registrations / updates
SELECT
    COUNT(*) AS total_property_ownerships,
    COUNT(*) FILTER (WHERE created_date >= NOW() - INTERVAL '14 DAYS') AS new_property_ownerships_last_2_weeks,
    COUNT(*) FILTER (WHERE last_modified_date >= NOW() - INTERVAL '14 DAYS') AS updated_property_ownerships_last_2_weeks
FROM
    property_ownership;

-- Compliances added / updated
SELECT
    COUNT(*) AS total_property_compliances,
    COUNT(*) FILTER (WHERE created_date >= NOW() - INTERVAL '14 DAYS') AS new_property_compliances_last_2_weeks,
    COUNT(*) FILTER (WHERE last_modified_date >= NOW() - INTERVAL '14 DAYS') AS updated_property_compliances_last_2_weeks
FROM
    property_compliance;

-- Local council users added / updated
SELECT
    COUNT(*) AS total_lc_users,
    COUNT(*) FILTER (WHERE created_date >= NOW() - INTERVAL '14 DAYS') AS new_lc_users_last_2_weeks,
    COUNT(*) FILTER (WHERE last_modified_date >= NOW() - INTERVAL '14 DAYS') AS updated_lc_users_last_2_weeks
FROM
    local_authority_user;