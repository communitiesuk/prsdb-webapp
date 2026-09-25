ALTER TABLE property_ownership ADD COLUMN renewal_date DATE;

-- Backfill renewal_date to match RenewalDateHelper.getRenewalDate: the next occurrence, strictly after today (UK time),
-- of the anniversary of the landlord who registered the property.
WITH earliest_landlord_anniversary AS (
    -- The earliest ownership link is the registering landlord, unless they have since left or deregistered.
    -- Landlords without an anniversary are skipped. Joint landlords don't get one when they join a property,
    -- only when they register a property themselves.
    -- ol.id breaks ties between links created at the same time.
    SELECT DISTINCT ON (ol.landlordship_id)
           ol.landlordship_id AS property_ownership_id,
           l.anniversary_day,
           l.anniversary_month
    FROM ownership_link ol
    JOIN landlord l ON l.id = ol.landlord_id
    WHERE l.anniversary_day IS NOT NULL
    ORDER BY ol.landlordship_id, ol.created_date, ol.id
),
anniversary AS (
    -- If no current landlord has an anniversary, use the property's own registration date,
    -- as that's what a landlord's first registration sets their anniversary to.
    SELECT po.id AS property_ownership_id,
           COALESCE(ela.anniversary_day, EXTRACT(DAY FROM po.created_date AT TIME ZONE 'Europe/London')::int) AS day,
           COALESCE(ela.anniversary_month, EXTRACT(MONTH FROM po.created_date AT TIME ZONE 'Europe/London')::int) AS month
    FROM property_ownership po
    LEFT JOIN earliest_landlord_anniversary ela ON ela.property_ownership_id = po.id
),
uk_today AS (
    -- Anniversaries are UK dates, so compare against today in the UK (as RenewalDateHelper does).
    -- now()::date would use the connection's time zone instead.
    SELECT (now() AT TIME ZONE 'Europe/London')::date AS today
),
next_anniversary AS (
    -- This year if the anniversary is still to come, otherwise next year.
    -- An anniversary of today goes to next year, as the renewal date must be after today.
    SELECT a.property_ownership_id,
           a.day,
           a.month,
           EXTRACT(YEAR FROM t.today)::int
               + CASE WHEN (a.month, a.day) <= (EXTRACT(MONTH FROM t.today), EXTRACT(DAY FROM t.today)) THEN 1 ELSE 0 END AS year
    FROM anniversary a
    CROSS JOIN uk_today t
)
UPDATE property_ownership po
SET renewal_date = CASE
        -- make_date errors for 29 Feb in a non-leap year, so use 28 Feb + 1 day instead.
        -- This gives 29 Feb in leap years and 1 Mar otherwise, as RenewalDateHelper does.
        WHEN na.month = 2 AND na.day = 29 THEN make_date(na.year, 2, 28) + 1
        ELSE make_date(na.year, na.month, na.day)
    END
FROM next_anniversary na
WHERE na.property_ownership_id = po.id;

ALTER TABLE property_ownership ALTER COLUMN renewal_date SET NOT NULL;
