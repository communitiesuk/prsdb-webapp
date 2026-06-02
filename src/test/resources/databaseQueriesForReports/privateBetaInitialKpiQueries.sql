-- A :reference_date of CURRENT_DATE will count records between just before midnight last night and 20th may.

-- Landlord registrations
SELECT
        COUNT(*) FILTER (WHERE created_date < :reference_date and created_date >= make_date(2026, 5, 20)) AS total_landlords,
        COUNT(*) FILTER (WHERE created_date < :reference_date and created_date >= make_date(2026, 5, 20) and is_verified = true) AS verified_landlords
FROM
    landlord;


-- Landlord with at least one property ownership
SELECT COUNT(DISTINCT po.primary_landlord_id) AS landlords_with_properties
FROM property_ownership po
         JOIN landlord l ON l.id = po.primary_landlord_id
WHERE l.created_date <  :reference_date
  AND l.created_date >= make_date(2026, 5, 20);


-- Property registrations
SELECT
        COUNT(*) FILTER (WHERE created_date < :reference_date and created_date >= make_date(2026, 5, 20)) AS total_property_ownerships
FROM
    property_ownership;


-- Passcodes claimed
SELECT
        COUNT(*) FILTER (WHERE created_date < :reference_date and created_date >= make_date(2026, 5, 20)) AS total_passcodes,
        COUNT(DISTINCT subject_identifier) FILTER (WHERE last_modified_date < :reference_date and last_modified_date >= make_date(2026, 5, 20)) AS claimed_passcodes
FROM
    passcode;

-- Time to register first property (all records)
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
