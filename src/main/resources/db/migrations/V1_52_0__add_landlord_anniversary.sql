ALTER TABLE landlord ADD COLUMN anniversary_day INTEGER;
ALTER TABLE landlord ADD COLUMN anniversary_month INTEGER;

ALTER TABLE landlord ADD CONSTRAINT chk_landlord_anniversary_completeness
    CHECK ((anniversary_day IS NULL) = (anniversary_month IS NULL));

UPDATE landlord l
SET anniversary_day = EXTRACT(DAY FROM sub.first_registration_date)::int,
    anniversary_month = EXTRACT(MONTH FROM sub.first_registration_date)::int
FROM (
    SELECT ol.landlord_id,
           MIN((po.created_date AT TIME ZONE 'Europe/London')::date) AS first_registration_date
    FROM ownership_link ol
    JOIN property_ownership po ON po.id = ol.landlordship_id
    GROUP BY ol.landlord_id
) sub
WHERE l.id = sub.landlord_id;
