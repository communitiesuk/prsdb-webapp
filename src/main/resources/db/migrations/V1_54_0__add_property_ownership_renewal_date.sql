ALTER TABLE property_ownership ADD COLUMN renewal_date DATE;

WITH earliest_landlord_anniversary AS (
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
    SELECT po.id AS property_ownership_id,
           COALESCE(ela.anniversary_day, EXTRACT(DAY FROM po.created_date AT TIME ZONE 'Europe/London')::int) AS day,
           COALESCE(ela.anniversary_month, EXTRACT(MONTH FROM po.created_date AT TIME ZONE 'Europe/London')::int) AS month
    FROM property_ownership po
    LEFT JOIN earliest_landlord_anniversary ela ON ela.property_ownership_id = po.id
),
uk_today AS (
    SELECT (now() AT TIME ZONE 'Europe/London')::date AS today
),
next_anniversary AS (
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
        WHEN na.month = 2 AND na.day = 29 THEN make_date(na.year, 2, 28) + 1
        ELSE make_date(na.year, na.month, na.day)
    END
FROM next_anniversary na
WHERE na.property_ownership_id = po.id;

ALTER TABLE property_ownership ALTER COLUMN renewal_date SET NOT NULL;
