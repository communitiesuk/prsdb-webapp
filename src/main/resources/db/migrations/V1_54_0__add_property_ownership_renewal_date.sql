ALTER TABLE property_ownership ADD COLUMN renewal_date DATE;

WITH picked AS (
    SELECT po.id AS po_id,
           l.anniversary_day AS anniversary_day,
           l.anniversary_month AS anniversary_month
    FROM property_ownership po
    JOIN LATERAL (
        SELECT la.anniversary_day, la.anniversary_month
        FROM ownership_link ol
        JOIN landlord la ON la.id = ol.landlord_id
        WHERE ol.landlordship_id = po.id
          AND la.anniversary_day IS NOT NULL
          AND la.anniversary_month IS NOT NULL
        ORDER BY ol.landlord_id
        LIMIT 1
    ) l ON true
),
leap_year_adjusted_anniversaries AS (
    SELECT picked.po_id,
           CASE
               WHEN anniversary_month = 2 AND anniversary_day = 29
                    AND NOT (year % 4 = 0 AND (year % 100 <> 0 OR year % 400 = 0))
                   THEN make_date(year, 3, 1)
               ELSE make_date(year, anniversary_month, anniversary_day)
           END AS anniversary_date
    FROM picked
    CROSS JOIN generate_series(
        EXTRACT(YEAR FROM current_date)::int,
        EXTRACT(YEAR FROM current_date)::int + 1
    ) AS year
),
renewal AS (
    SELECT po_id, MIN(anniversary_date) AS renewal_date
    FROM leap_year_adjusted_anniversaries
    WHERE anniversary_date > current_date
    GROUP BY po_id
)
UPDATE property_ownership po
SET renewal_date = renewal.renewal_date
FROM renewal
WHERE po.id = renewal.po_id;

ALTER TABLE property_ownership ALTER COLUMN renewal_date SET NOT NULL;
