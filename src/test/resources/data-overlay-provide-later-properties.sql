-- Sets up five eligible properties, a cutoff-boundary property, a younger property, an inactive property, and an old property without provide-later flags.
-- Apply on top of data-mockuser-landlord-with-many-compliance-actions.sql.
-- Eligible properties 1-5 cover licensing, tenancy, gas, electrical, and EPC respectively; property 1 has no compliance row.

UPDATE property_ownership
SET created_date = CASE
        WHEN id BETWEEN 1 AND 5 THEN
            (date_trunc('day', current_timestamp AT TIME ZONE 'Europe/London') - interval '30 days') AT TIME ZONE 'Europe/London'
        WHEN id = 6 THEN
            (date_trunc('day', current_timestamp AT TIME ZONE 'Europe/London') - interval '28 days') AT TIME ZONE 'Europe/London'
        WHEN id = 7 THEN
            (date_trunc('day', current_timestamp AT TIME ZONE 'Europe/London') - interval '27 days') AT TIME ZONE 'Europe/London'
        ELSE
            (date_trunc('day', current_timestamp AT TIME ZONE 'Europe/London') - interval '30 days') AT TIME ZONE 'Europe/London'
    END,
    is_active = id <> 8,
    license_provide_later = id IN (1, 6),
    tenancy_provide_later = id IN (2, 8);

DELETE FROM property_compliance WHERE property_ownership_id = 1;

UPDATE property_compliance
SET gas_safety_cert_provide_later = property_ownership_id IN (3, 7),
    electrical_safety_cert_provide_later = property_ownership_id = 4,
    epc_provide_later = property_ownership_id = 5;
