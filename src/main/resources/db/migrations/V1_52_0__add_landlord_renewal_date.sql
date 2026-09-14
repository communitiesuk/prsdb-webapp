ALTER TABLE landlord ADD COLUMN renewal_date DATE;

UPDATE landlord l
SET renewal_date = sub.first_registration_date
FROM (
    SELECT ol.landlord_id,
           MIN((po.created_date AT TIME ZONE 'Europe/London')::date) AS first_registration_date
    FROM ownership_link ol
    JOIN property_ownership po ON po.id = ol.landlordship_id
    GROUP BY ol.landlord_id
) sub
WHERE l.id = sub.landlord_id;
