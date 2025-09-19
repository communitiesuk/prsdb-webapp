SELECT
    percentile_cont(0.50) WITHIN GROUP (ORDER BY time_to_register_first_property) AS p50_time_to_register_first_property,
    percentile_cont(0.95) WITHIN GROUP (ORDER BY time_to_register_first_property) AS p95_time_to_register_first_property,
    percentile_cont(0.99) WITHIN GROUP (ORDER BY time_to_register_first_property) AS p99_time_to_register_first_property
FROM (
     SELECT
         po.min_created_date - l.created_date AS time_to_register_first_property
     FROM
         landlord l
             JOIN (
             SELECT
                 primary_landlord_id,
                 MIN(created_date) AS min_created_date
             FROM
                 property_ownership
             GROUP BY
                 primary_landlord_id
         ) po ON l.id = po.primary_landlord_id
 ) t