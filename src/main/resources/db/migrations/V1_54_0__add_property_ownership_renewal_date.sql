ALTER TABLE property_ownership ADD COLUMN renewal_date DATE;

-- Backfill with the renewal date RenewalDateHelper would have set at registration.
WITH registering_landlord_anniversary AS (
    -- The earliest linked landlord with an anniversary, i.e. the registering landlord unless they've left.
    SELECT DISTINCT ON (ol.landlordship_id)
           ol.landlordship_id AS property_ownership_id,
           l.anniversary_month,
           l.anniversary_day
    FROM ownership_link ol
    JOIN landlord l ON l.id = ol.landlord_id
    WHERE l.anniversary_day IS NOT NULL
    ORDER BY ol.landlordship_id, ol.created_date, ol.id
),
property_anniversary AS (
    -- Fall back to the registration date if no linked landlord has an anniversary.
    SELECT po.id AS property_ownership_id,
           r.registration_date,
           COALESCE(rla.anniversary_month, EXTRACT(MONTH FROM r.registration_date)::int) AS month,
           COALESCE(rla.anniversary_day, EXTRACT(DAY FROM r.registration_date)::int) AS day
    FROM property_ownership po
    CROSS JOIN LATERAL (SELECT (po.created_date AT TIME ZONE 'Europe/London')::date AS registration_date) r
    LEFT JOIN registering_landlord_anniversary rla ON rla.property_ownership_id = po.id
)
UPDATE property_ownership po
-- The next anniversary after registration. Building from the 1st of the month turns 29 Feb into 1 Mar in non-leap years.
SET renewal_date = make_date(
        EXTRACT(YEAR FROM pa.registration_date)::int
            + CASE WHEN (pa.month, pa.day) > (EXTRACT(MONTH FROM pa.registration_date), EXTRACT(DAY FROM pa.registration_date))
                THEN 0 ELSE 1 END,
        pa.month,
        1
    ) + (pa.day - 1)
FROM property_anniversary pa
WHERE pa.property_ownership_id = po.id;

ALTER TABLE property_ownership ALTER COLUMN renewal_date SET NOT NULL;
