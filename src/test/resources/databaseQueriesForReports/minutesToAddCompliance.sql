SELECT
            percentile_cont(0.50) WITHIN GROUP (ORDER BY minutes_to_add_compliance) AS p50_minutes_to_add_compliance,
            percentile_cont(0.95) WITHIN GROUP (ORDER BY minutes_to_add_compliance) AS p95_minutes_to_add_compliance,
            percentile_cont(0.99) WITHIN GROUP (ORDER BY minutes_to_add_compliance) AS p99_minutes_to_add_compliance
FROM (
         SELECT
             EXTRACT(EPOCH FROM (pc.created_date - po.created_date)) / 60 AS minutes_to_add_compliance
         FROM
             property_ownership po
                 JOIN (
                 SELECT
                     property_ownership_id,
                     created_date
                 FROM
                     property_compliance
             ) pc ON po.id = pc.property_ownership_id
     ) t