ALTER TABLE property_ownership ADD COLUMN renewal_date DATE;

WITH picked AS (
    SELECT po.id AS po_id,
           l.anniversary_day AS anniversary_day,
           l.anniversary_month AS anniversary_month
    FROM property_ownership po
    JOIN LATERAL (
        SELECT l.anniversary_day, l.anniversary_month
        FROM ownership_link ol
        JOIN landlord l ON l.id = ol.landlord_id
        WHERE ol.landlordship_id = po.id
          AND l.anniversary_day IS NOT NULL
          AND l.anniversary_month IS NOT NULL
        ORDER BY ol.landlord_id
        LIMIT 1
    ) l ON true
)
UPDATE property_ownership po
SET renewal_date =
        CASE
            WHEN picked.anniversary_month = 2 AND picked.anniversary_day = 29
                THEN make_date(2027, 3, 1) -- 2027 is not a leap year, so 29 Feb maps to 1 Mar
            ELSE make_date(2027, picked.anniversary_month, picked.anniversary_day)
        END
FROM picked
WHERE po.id = picked.po_id;

ALTER TABLE property_ownership ALTER COLUMN renewal_date SET NOT NULL;
