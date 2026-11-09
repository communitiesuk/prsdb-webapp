-- Change property ownership 6 to an EIC on top of data-mockuser-landlord-with-compliance-actions.sql.
UPDATE property_compliance
SET electrical_cert_type = 1,
    eic_issue_date = (eicr_expiry_date - interval '5 years')::date,
    eicr_expiry_date = null
WHERE property_ownership_id = 6;
