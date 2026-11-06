-- Read-only export for OrganisationStructureSimulation. Skips the first 347 eligible organisations, which cover every
-- organisation allocated to the profile and operations simulations, then gives each structure operation its own
-- disjoint pool of 101.
WITH eligible AS (
    SELECT DISTINCT ON (l.id)
           u.subject_identifier AS subject,
           l.id AS "organisationId",
           r.number AS "registrationNumber",
           l.organisation_landlord_name AS name,
           l.organisation_is_trust AS trust,
           l.organisation_company_number IS NOT NULL AS company,
           l.organisation_lead_trustee_name IS NOT NULL AS "hasLeadTrustee"
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
    OFFSET 347
),
categorised AS (
    SELECT eligible.*,
           CASE
               WHEN trust AND "hasLeadTrustee" THEN 'trust'
               WHEN NOT trust AND NOT company THEN 'non-company'
               WHEN NOT trust AND company THEN 'company'
           END AS category
    FROM eligible
),
ranked AS (
    SELECT categorised.*, row_number() OVER (PARTITION BY category ORDER BY "organisationId") AS position
    FROM categorised
    WHERE category IS NOT NULL
),
allocated AS (
    SELECT CASE
               WHEN category = 'trust' AND position <= 101 THEN 'lead-trustee'
               WHEN category = 'trust' AND position <= 202 THEN 'remove-trust'
               WHEN category = 'non-company' AND position <= 101 THEN 'governing-body'
               WHEN category = 'non-company' AND position <= 202 THEN 'to-company'
               WHEN category = 'non-company' AND position <= 303 THEN 'add-trust'
               WHEN category = 'company' AND position <= 101 THEN 'company-number'
               WHEN category = 'company' AND position <= 202 THEN 'to-non-company'
           END AS operation,
           category, subject, "organisationId", "registrationNumber", name
    FROM ranked
)
SELECT json_object_agg(operation, fixtures)
FROM (
    SELECT operation,
           json_agg(json_build_object(
               'subject', subject,
               'organisationId', "organisationId",
               'registrationNumber', "registrationNumber",
               'name', name
           ) ORDER BY "organisationId") AS fixtures
    FROM allocated
    WHERE operation IS NOT NULL
      -- The governing body journeys start from the stored member list, which only organisations without a company number keep.
      AND EXISTS (
          SELECT 1 FROM organisation_governing_body_member m WHERE m.organisation_landlord_id = allocated."organisationId"
      ) = (category <> 'company')
    GROUP BY operation
) pools;
