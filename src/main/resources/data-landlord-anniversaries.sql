-- Gives every landlord linked to a property an anniversary: the date they were first linked to one
UPDATE landlord l
SET anniversary_day = EXTRACT(DAY FROM sub.first_link_date)::int,
    anniversary_month = EXTRACT(MONTH FROM sub.first_link_date)::int
FROM (
    SELECT ol.landlord_id,
           MIN((ol.created_date AT TIME ZONE 'Europe/London')::date) AS first_link_date
    FROM ownership_link ol
    GROUP BY ol.landlord_id
) sub
WHERE l.id = sub.landlord_id
  AND l.anniversary_day IS NULL;
