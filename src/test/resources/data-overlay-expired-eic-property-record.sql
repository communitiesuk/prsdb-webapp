-- Give occupied property ownership 2 an expired EIC on top of data-mockuser-landlord-with-properties.sql.
UPDATE property_compliance
SET electrical_cert_type = 1,
    eic_issue_date = ((current_date - 1) - interval '5 years')::date,
    eicr_expiry_date = null
WHERE property_ownership_id = 2;
