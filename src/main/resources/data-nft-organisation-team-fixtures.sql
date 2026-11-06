-- Read-only export for OrganisationOperationsSimulation's team membership operations. Allocates organisations from the
-- highest ids downwards so they stay clear of the profile, operations and structure pools, which are allocated upwards.
-- Each operation has its own disjoint pool of 101 organisations within the exported array:
--   accept              fixtureOffset 0   one pending (unexpired, not hidden) seeded invitation per organisation
--   change-access-level fixtureOffset 101 an administrator plus at least one editor, who is promoted to administrator
--   remove              fixtureOffset 202 an administrator plus at least one editor, who is removed from the organisation
-- Invitations created by the team operation (org-team-*) are excluded so its audits stay valid.
WITH admins AS (
    SELECT DISTINCT ON (l.id)
           u.subject_identifier AS subject,
           l.id AS "organisationId",
           r.number AS "registrationNumber",
           l.organisation_landlord_name AS name
    FROM organisational_landlord_user u
    JOIN landlord l ON l.id = u.organisation_landlord_id
    JOIN registration_number r ON r.id = l.registration_number_id
    WHERE l.landlord_type = 1 AND l.id > 26 AND u.role = 0
      AND u.subject_identifier LIKE 'urn:fdc:gov.uk:2022:%'
      AND NOT EXISTS (
          SELECT 1 FROM landlord individual
          WHERE individual.individual_subject_identifier = u.subject_identifier
      )
    ORDER BY l.id, u.id
),
accept AS (
    SELECT DISTINCT ON (a."organisationId")
           a.subject, a."organisationId", a."registrationNumber", a.name, v.token::text AS "invitationToken"
    FROM admins a
    JOIN organisational_landlord_invitation v ON v.organisation_landlord_id = a."organisationId"
    WHERE NOT v.is_hidden
      AND v.created_date >= current_date - 27
      AND v.invited_email NOT LIKE 'org-team-%'
    ORDER BY a."organisationId" DESC, v.id
    LIMIT 101
),
members AS (
    SELECT a.subject, a."organisationId", a."registrationNumber", a.name
    FROM admins a
    WHERE EXISTS (
        SELECT 1 FROM organisational_landlord_user m
        WHERE m.organisation_landlord_id = a."organisationId" AND m.role = 1
    )
      AND a."organisationId" NOT IN (SELECT "organisationId" FROM accept)
    ORDER BY a."organisationId" DESC
    LIMIT 202
),
fixtures AS (
    SELECT 0 AS pool, "organisationId", to_json(accept) AS fixture FROM accept
    UNION ALL
    SELECT 1, "organisationId", to_json(members) FROM members
)
SELECT COALESCE(json_agg(fixture ORDER BY pool, "organisationId" DESC), '[]'::json) FROM fixtures;
