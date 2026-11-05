-- Read-only. Only existing synthetic invitations and non-council subjects are
-- exported. A run consumes both invitations; re-export before every run.
WITH subjects AS (
    SELECT u.id AS subject, row_number() OVER (ORDER BY u.id) AS ordinal
    FROM prsdb_user u
    WHERE u.id LIKE 'urn:fdc:gov.uk:2022:%'
      AND NOT EXISTS (SELECT 1 FROM local_council_user c WHERE c.subject_identifier = u.id)
      AND NOT EXISTS (SELECT 1 FROM landlord l WHERE l.individual_subject_identifier = u.id)
      AND NOT EXISTS (SELECT 1 FROM system_operator o WHERE o.subject_identifier = u.id)
      AND NOT EXISTS (SELECT 1 FROM organisational_landlord_user o WHERE o.subject_identifier = u.id)
    ORDER BY u.id LIMIT 101
), valid AS (
    SELECT i.token::text AS "validToken", i.invited_email AS email,
           i.created_date + interval '48 hours' AS "validUntil",
           c.name AS "councilName", i.invited_as_admin AS "invitedAsAdmin",
           row_number() OVER (ORDER BY i.id DESC) AS ordinal
    FROM local_council_invitation i
    JOIN local_council c ON c.id = i.inviting_council_id
    WHERE i.invited_email LIKE '%@example.com'
      AND i.invited_email LIKE '000-gatling-registration-%@example.com'
      AND i.created_date > current_timestamp - interval '47 hours 50 minutes'
    ORDER BY i.id DESC LIMIT 101
), expired AS (
    SELECT i.token::text AS "expiredToken", row_number() OVER (ORDER BY i.id) AS ordinal
    FROM local_council_invitation i
    WHERE i.invited_email LIKE '%@example.com'
      AND i.created_date < current_timestamp - interval '48 hours'
    ORDER BY i.id LIMIT 101
), fixtures AS (
    SELECT s.subject, v."validToken", v.email, v."validUntil",
           v."councilName", v."invitedAsAdmin", e."expiredToken"
    FROM subjects s JOIN valid v USING (ordinal) JOIN expired e USING (ordinal)
)
SELECT COALESCE(json_agg(fixtures ORDER BY subject), '[]'::json) FROM fixtures;
