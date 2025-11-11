-- We count the start of landlord registration as the time when the landlord's one-login user account was created.'
SELECT
    percentile_cont(0.50) WITHIN GROUP (ORDER BY time_to_register_landlord) AS p50_time_to_register_landlord,
    percentile_cont(0.95) WITHIN GROUP (ORDER BY time_to_register_landlord) AS p95_time_to_register_landlord,
    percentile_cont(0.99) WITHIN GROUP (ORDER BY time_to_register_landlord) AS p99_time_to_register_landlord
FROM (
     SELECT
         l.created_date - olu.created_date AS time_to_register_landlord
     -- To get in minutes instead, use
     -- EXTRACT(EPOCH FROM (p.last_modified_date - l.created_date)) / 60 AS time_to_register_landlord
     FROM
         landlord l
             JOIN (
             SELECT
                 id,
                 created_date
             FROM
                 one_login_user
         ) olu ON olu.id = l.subject_identifier
 ) t